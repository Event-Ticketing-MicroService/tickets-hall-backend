package com.ticketshall.tickets.service.impl;

import com.ticketshall.tickets.dto.ReservationRequest;
import com.ticketshall.tickets.exceptions.TicketTypeLockTimeoutException;
import com.ticketshall.tickets.exceptions.TicketTypeNotFoundException;
import com.ticketshall.tickets.exceptions.TicketTypeStockNotEnoughException;
import com.ticketshall.tickets.models.TicketType;
import com.ticketshall.tickets.models.nonStoredModels.Reservation;
import com.ticketshall.tickets.models.nonStoredModels.ReservationItem;
import com.ticketshall.tickets.repository.EventRepository;
import com.ticketshall.tickets.repository.TicketRepository;
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
    private final TicketRepository ticketRepository;
    private final TicketTypeRepository  ticketTypeRepository;
    private final EventRepository eventRepository;
    private final RedissonClient redissonClient;
    private static final Duration EXPIRATION_TIME = Duration.ofMinutes(10);
    private static final Duration TTL = Duration.ofMinutes(12);
    private static final String RESERVATION_PREFIX = "reservation:";
    private static final String TICKET_TYPE_PREFIX = "ticketType:";
    @Override
    public Reservation reserve(ReservationRequest reservationRequest) {
        String reservationId = UUID.randomUUID().toString();
        List<ReservationItem> reservationItems = new ArrayList<>();
        float totalPrice = (float) 0;
        for(var requestItem: reservationRequest.items()){
            String ticketTypeKey =
                    String.format("%s%s", TICKET_TYPE_PREFIX, requestItem.ticketTypeId().toString());
            RLock lock = redissonClient.getLock("lock:" + ticketTypeKey);
            try{
                if(!lock.tryLock(5,10, TimeUnit.SECONDS)){
                    throw new TicketTypeLockTimeoutException
                            ("Could not lock TicketType " + requestItem.ticketTypeId());
                }
                Map<Object, Object> ticketData = redisTemplate.opsForHash().entries(ticketTypeKey);
                if (ticketData.isEmpty()) {
                    TicketType type = ticketTypeRepository
                            .findById(requestItem.ticketTypeId())
                            .orElseThrow(() -> new TicketTypeNotFoundException
                                    ("TicketType not found: " + requestItem.ticketTypeId()));

                    ticketData = Map.of(
                            "id", String.valueOf(type.getId()),
                            "eventId", String.valueOf(type.getEventId()),
                            "name", type.getName(),
                            "price", type.getPrice().toString(),
                            "totalStock", String.valueOf(type.getTotalStock()),
                            "availableStock", String.valueOf(type.getAvailableStock())
                    );

                    redisTemplate.opsForHash().putAll(ticketTypeKey, ticketData);
                }
                String name = (String) ticketData.get("name");
                float price = Float.parseFloat((String)ticketData.get("price"));
                int available = Integer.parseInt((String) ticketData.get("availableStock"));
                if(available < requestItem.quantity()){
                    throw new TicketTypeStockNotEnoughException("Not enough stock for TicketType " + requestItem.ticketTypeId());
                }
                redisTemplate
                        .opsForHash()
                        .put(ticketTypeKey, "availableStock", available - requestItem.quantity());
                reservationItems.add(new ReservationItem(requestItem.ticketTypeId(), name, requestItem.quantity(), price));
                totalPrice += requestItem.quantity() * price;
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lock interrupted for ticket type: " + requestItem.ticketTypeId(), e);
            }
            finally {
                lock.unlock();
            }
        }
        Reservation reservation = new Reservation(
                UUID.fromString(reservationId),
                reservationRequest.attendeeId(),
                reservationRequest.eventId(),
                reservationItems,
                totalPrice,
                LocalDateTime.now().plus(EXPIRATION_TIME)
        );
        String reservationKey = String.format("%s%s", RESERVATION_PREFIX, reservationId);
        redisTemplate.opsForValue().set(reservationKey, reservationRequest, TTL);
        return reservation;
    }

    public void expireReservation(String reservationId) {
        String redisKey = String.format("%s%s", RESERVATION_PREFIX, reservationId);

        Reservation reservation = (Reservation) redisTemplate.opsForValue().get(redisKey);
        if (reservation == null) {
            return;
        }

        for (ReservationItem item : reservation.getItems()) {
            String ticketTypeKey = String.format("%s%s", TICKET_TYPE_PREFIX, item.getTicketTypeId());
            String lockKey = "lock:" + ticketTypeKey;
            RLock lock = redissonClient.getLock(lockKey);

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
