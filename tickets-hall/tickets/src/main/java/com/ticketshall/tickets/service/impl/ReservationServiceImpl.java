package com.ticketshall.tickets.service.impl;

import com.ticketshall.tickets.dto.request.ReservationRequest;
import com.ticketshall.tickets.exceptions.TicketTypeLockTimeoutException;
import com.ticketshall.tickets.exceptions.TicketTypeNotFoundException;
import com.ticketshall.tickets.exceptions.TicketTypeStockNotEnoughException;
import com.ticketshall.tickets.models.TicketType;
import com.ticketshall.tickets.models.nonStoredModels.Reservation;
import com.ticketshall.tickets.models.nonStoredModels.ReservationItem;
import com.ticketshall.tickets.models.nonStoredModels.constants.GeneralConstants;
import com.ticketshall.tickets.repository.TicketTypeRepository;
import com.ticketshall.tickets.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final TicketTypeRepository ticketTypeRepository;
    private final RedissonClient redissonClient;
    private static final Duration EXPIRATION_TIME = Duration.ofMinutes(10);
    private static final Duration TTL = Duration.ofMinutes(12);

    @Override
    public Reservation reserve(ReservationRequest request) {
        List<ReservationItem> reservationItems = new ArrayList<>();
        float totalPrice = 0;

        for (var reqItem : request.items()) {
            String ticketTypeKey = buildTicketTypeKey(request.eventId(), reqItem.ticketTypeId());
            RLock lock = redissonClient.getLock("lock:" + ticketTypeKey);

            try {
                // acquire lock
                if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                    throw new TicketTypeLockTimeoutException("Could not lock TicketType " + reqItem.ticketTypeId());
                }
                TicketType ticketType = getOrLoadTicketType(ticketTypeKey,
                        reqItem.ticketTypeId(),
                        request.eventId());
                if (ticketType.getAvailableStock() < reqItem.quantity()) {
                    throw new TicketTypeStockNotEnoughException("Not enough stock for " + ticketType.getName());
                }

                ticketType.setAvailableStock(ticketType.getAvailableStock() - reqItem.quantity());
                updateTicketTypeCache(request.eventId(), ticketType);

                ReservationItem item = new ReservationItem(
                        ticketType.getId(),
                        ticketType.getName(),
                        reqItem.quantity(),
                        ticketType.getPrice()
                );

                reservationItems.add(item);
                totalPrice += item.getQuantity() * item.getUnitPrice();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lock interrupted for ticket type: " + reqItem.ticketTypeId(), e);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        return createAndStoreReservation(request, reservationItems, totalPrice);
    }

    private String buildTicketTypeKey(UUID eventId, UUID ticketTypeId) {
        return String.format("%s%s%s%s",
                GeneralConstants.REDIS_EVENT_PREFIX,
                eventId,
                GeneralConstants.REDIS_TICKET_TYPE_INFIX,
                ticketTypeId);
    }
    private String getEventTicketTypesKey(UUID eventId) {
        return GeneralConstants.REDIS_EVENT_PREFIX + eventId + GeneralConstants.REDIS_TICKET_TYPE_INFIX;
    }
    private TicketType getOrLoadTicketType(String key, UUID ticketTypeId, UUID eventId) {
        RMap<String, String> hashMap = redissonClient.getMap(key);

        String cacheKey = getEventTicketTypesKey(eventId);
        RList<TicketType> cachedList = redissonClient.getList(cacheKey);
        // builds the cacheList if not exist or empty
        if (!cachedList.isExists() && cachedList.isEmpty()) {
            List<TicketType> ticketTypes = ticketTypeRepository.getTicketTypesByEventId(eventId);
            if(!ticketTypes.isEmpty()) {
                cachedList.addAll(ticketTypes);
            }
        }

        if (hashMap.isEmpty()) {
            TicketType type = ticketTypeRepository.findById(ticketTypeId)
                    .orElseThrow(() -> new TicketTypeNotFoundException("TicketType not found: " + ticketTypeId));

            cacheTicketTypeHash(key, type);
            return type;
        } else {
            return mapToTicketType(hashMap);
        }
    }

    private void cacheTicketTypeHash(String key, TicketType type) {
        RMap<String, String> hashMap = redissonClient.getMap(key);
        Map<String, String> map = new HashMap<>();
        map.put("id", type.getId().toString());
        map.put("eventId", type.getEventId().toString());
        map.put("name", type.getName());
        map.put("price", type.getPrice().toString());
        map.put("totalStock", String.valueOf(type.getTotalStock()));
        map.put("availableStock", String.valueOf(type.getAvailableStock()));
        hashMap.putAll(map);
    }

    private TicketType mapToTicketType(RMap<String, String> data) {
        TicketType type = new TicketType();
        type.setId(UUID.fromString(data.get("id")));
        type.setEventId(UUID.fromString(data.get("eventId")));
        type.setName(data.get("name"));
        type.setPrice(Float.parseFloat(data.get("price")));
        type.setTotalStock(Integer.parseInt(data.get("totalStock")));
        type.setAvailableStock(Integer.parseInt(data.get("availableStock")));
        return type;
    }

    private void updateTicketTypeCache(UUID eventId, TicketType updated) {
        // Update hash
        String hashKey = buildTicketTypeKey(eventId, updated.getId());
        cacheTicketTypeHash(hashKey, updated);

        // Update list (consistent with TicketTypeServiceImpl)
        String listKey = GeneralConstants.REDIS_EVENT_PREFIX + eventId + GeneralConstants.REDIS_TICKET_TYPE_INFIX;
        RList<TicketType> cachedList = redissonClient.getList(listKey);

        if (cachedList.isExists() && !cachedList.isEmpty()) {
            List<TicketType> allTypes = cachedList.readAll();
            for (int i = 0; i < allTypes.size(); i++) {
                if (allTypes.get(i).getId().equals(updated.getId())) {
                    cachedList.set(i, updated);
                    break;
                }
            }
        }
    }

    private Reservation createAndStoreReservation(ReservationRequest request, List<ReservationItem> items, float totalPrice) {
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = new Reservation(
                reservationId,
                request.attendeeId(),
                request.eventId(),
                items,
                totalPrice,
                LocalDateTime.now().plus(EXPIRATION_TIME)
        );

        String reservationKey = String.format("%s%s", GeneralConstants.REDIS_RESERVATION_PREFIX, reservationId);
        RBucket<Reservation> bucket = redissonClient.getBucket(reservationKey);
        bucket.set(reservation, TTL);

        return reservation;
    }

    public void expireReservation(UUID reservationId) {
        String redisKey = String.format("%s%s", GeneralConstants.REDIS_RESERVATION_PREFIX, reservationId);
        RBucket<Reservation> bucket = redissonClient.getBucket(redisKey);
        Reservation reservation = bucket.get();

        if (reservation == null) return;

        for (ReservationItem item : reservation.getItems()) {
            String ticketTypeKey = buildTicketTypeKey(reservation.getEventId(), item.getTicketTypeId());

            RLock lock = redissonClient.getLock("lock:" + ticketTypeKey);
            try {
                if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) continue;

                TicketType ticketType = getOrLoadTicketType(ticketTypeKey,
                        item.getTicketTypeId(),
                        reservation.getEventId());
                ticketType.setAvailableStock(ticketType.getAvailableStock() + item.getQuantity());
                updateTicketTypeCache(reservation.getEventId(), ticketType);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lock interrupted for ticket type: " + item.getTicketTypeId(), e);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        bucket.delete();
    }
}