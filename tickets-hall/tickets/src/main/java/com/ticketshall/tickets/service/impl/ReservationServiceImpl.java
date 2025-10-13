package com.ticketshall.tickets.service.impl;

import com.ticketshall.tickets.dto.ReservationRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
                // load ticket data from redis or from database
                Map<Object, Object> ticketData = getOrLoadTicketType(ticketTypeKey, reqItem.ticketTypeId());
                // Decrement the stock
                decrementStock(ticketData, reqItem.quantity(), ticketTypeKey);
                // Create a reservation item
                ReservationItem reservationItem = createReservationItem(ticketData, reqItem.quantity());
                // add the reservation item to list of reservation items
                reservationItems.add(reservationItem);
                // accumulate total price
                totalPrice += reservationItem.getQuantity() * reservationItem.getUnitPrice();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lock interrupted for ticket type: " + reqItem.ticketTypeId(), e);
            } finally {
                lock.unlock();
            }
        }
        // return the reservation
        return createAndStoreReservation(request, reservationItems, totalPrice);
    }

    private String buildTicketTypeKey(UUID eventId, UUID ticketTypeId) {
        return String.format("%s%s%s%s",
                GeneralConstants.REDIS_EVENT_PREFIX,
                eventId,
                GeneralConstants.REDIS_TICKET_TYPE_INFIX,
                ticketTypeId);
    }

    private Map<Object, Object> getOrLoadTicketType(String key, UUID ticketTypeId) {
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
        if (data.isEmpty()) {
            TicketType type = ticketTypeRepository.findById(ticketTypeId)
                    .orElseThrow(() -> new TicketTypeNotFoundException("TicketType not found: " + ticketTypeId));

            data = Map.of(
                    "id", String.valueOf(type.getId()),
                    "eventId", String.valueOf(type.getEventId()),
                    "name", type.getName(),
                    "price", type.getPrice().toString(),
                    "totalStock", String.valueOf(type.getTotalStock()),
                    "availableStock", String.valueOf(type.getAvailableStock())
            );

            redisTemplate.opsForHash().putAll(key, data);
        }
        return data;
    }

    private void decrementStock(Map<Object, Object> ticketData, int quantity, String key) {
        int available = Integer.parseInt((String) ticketData.get("availableStock"));
        if (available < quantity) {
            throw new TicketTypeStockNotEnoughException("Not enough stock for TicketType " + ticketData.get("id"));
        }
        redisTemplate.opsForHash().put(key, "availableStock", available - quantity);
    }

    private ReservationItem createReservationItem(Map<Object, Object> ticketData, int quantity) {
        String name = (String) ticketData.get("name");
        float price = Float.parseFloat((String) ticketData.get("price"));
        UUID ticketTypeId = UUID.fromString((String) ticketData.get("id"));
        return new ReservationItem(ticketTypeId, name, quantity, price);
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
        if (reservation == null) {
            return;
        }

        for (ReservationItem item : reservation.getItems()) {
            String ticketTypeKey = buildTicketTypeKey(reservation.getEventId(), item.getTicketTypeId());

            RLock lock = redissonClient.getLock("lock:" + ticketTypeKey);
            try {
                if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                    continue;
                }

                Map<Object, Object> ticketData = redisTemplate.opsForHash().entries(ticketTypeKey);
                if (ticketData.isEmpty()) {
                    continue;
                }

                int available = Integer.parseInt((String) ticketData.get("availableStock"));
                int newAvailable = available + item.getQuantity();
                redisTemplate.opsForHash().put(ticketTypeKey, "availableStock", newAvailable);
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
