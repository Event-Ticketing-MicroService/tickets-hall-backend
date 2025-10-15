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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final TicketTypeRepository  ticketTypeRepository;
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
                TicketType ticketType = getOrLoadTicketType(ticketTypeKey, reqItem.ticketTypeId());
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
                lock.unlock();
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

    private TicketType getOrLoadTicketType(String key, UUID ticketTypeId) {
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
        if (data.isEmpty()) {
            TicketType type = ticketTypeRepository.findById(ticketTypeId)
                    .orElseThrow(() -> new TicketTypeNotFoundException("TicketType not found: " + ticketTypeId));

            cacheTicketTypeHash(key, type);
            return type;
        } else {
            return mapToTicketType(data);
        }

    }

    private void cacheTicketTypeHash(String key, TicketType type) {
        Map<String, String> map = new HashMap<>();
        map.put("id", type.getId().toString());
        map.put("eventId", type.getEventId().toString());
        map.put("name", type.getName());
        map.put("price", type.getPrice().toString());
        map.put("totalStock", String.valueOf(type.getTotalStock()));
        map.put("availableStock", String.valueOf(type.getAvailableStock()));
        redisTemplate.opsForHash().putAll(key, map);
    }

    private TicketType mapToTicketType(Map<Object, Object> data) {
        TicketType type = new TicketType();
        type.setId(UUID.fromString((String) data.get("id")));
        type.setEventId(UUID.fromString((String) data.get("eventId")));
        type.setName((String) data.get("name"));
        type.setPrice(Float.parseFloat((String) data.get("price")));
        type.setTotalStock(Integer.parseInt((String) data.get("totalStock")));
        type.setAvailableStock(Integer.parseInt((String) data.get("availableStock")));
        return type;
    }

    private void updateTicketTypeCache(UUID eventId, TicketType updated) {
        String hashKey = buildTicketTypeKey(eventId, updated.getId());
        cacheTicketTypeHash(hashKey, updated);
        String listKey = GeneralConstants.REDIS_EVENT_PREFIX + eventId + GeneralConstants.REDIS_TICKET_TYPE_INFIX;
        List<TicketType> cachedList = (List<TicketType>) redisTemplate.opsForValue().get(listKey);

        if (cachedList != null && !cachedList.isEmpty()) {
            List<TicketType> updatedList = cachedList.stream()
                    .map(t -> t.getId().equals(updated.getId()) ? updated : t)
                    .toList();
            redisTemplate.opsForValue().set(listKey, updatedList, Duration.ofMinutes(30));
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
        redisTemplate.opsForValue().set(reservationKey, reservation, TTL);
        return reservation;
    }

    public void expireReservation(UUID reservationId) {
        String redisKey = String.format("%s%s", GeneralConstants.REDIS_RESERVATION_PREFIX, reservationId);
        Reservation reservation = (Reservation) redisTemplate.opsForValue().get(redisKey);
        if (reservation == null) return;

        for (ReservationItem item : reservation.getItems()) {
            String ticketTypeKey = buildTicketTypeKey(reservation.getEventId(), item.getTicketTypeId());

            RLock lock = redissonClient.getLock("lock:" + ticketTypeKey);
            try {
                if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) continue;

                TicketType ticketType = getOrLoadTicketType(ticketTypeKey, item.getTicketTypeId());
                ticketType.setAvailableStock(ticketType.getAvailableStock() + item.getQuantity());
                updateTicketTypeCache(reservation.getEventId(), ticketType);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lock interrupted for ticket type: " + item.getTicketTypeId(), e);
            } finally {
                lock.unlock();
            }
        }

        redisTemplate.delete(redisKey);
    }
}
