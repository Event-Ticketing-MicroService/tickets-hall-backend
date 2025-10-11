package com.ticketshall.tickets.service.impl;

import com.ticketshall.tickets.dto.ReservationRequest;
import com.ticketshall.tickets.models.TicketType;
import com.ticketshall.tickets.models.nonStoredModels.Reservation;
import com.ticketshall.tickets.models.nonStoredModels.ReservationItem;
import com.ticketshall.tickets.repository.EventRepository;
import com.ticketshall.tickets.repository.TicketRepository;
import com.ticketshall.tickets.repository.TicketTypeRepository;
import com.ticketshall.tickets.service.ReservationService;
import lombok.AllArgsConstructor;
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
    public Reservation reserve(ReservationRequest reservation) {
        String reservationId = UUID.randomUUID().toString();
        List<ReservationItem> items = new ArrayList<>();
        float totalPrice = (float) 0;
        for(var item: reservation.items()){
            String ticketTypeKey =
                    String.format("%s%s", TICKET_TYPE_PREFIX, item.ticketTypeId().toString());
            RLock lock = redissonClient.getLock("lock:" + ticketTypeKey);
            try{
                if(!lock.tryLock(5,10, TimeUnit.SECONDS)){
                    throw new RuntimeException
                            ("Could not lock TicketType " + item.ticketTypeId());
                }
                Map<Object, Object> ticketData = redisTemplate.opsForHash().entries(ticketTypeKey);
                if (ticketData.isEmpty()) {
                    TicketType type = ticketTypeRepository
                            .findById(item.ticketTypeId())
                            .orElseThrow(() -> new RuntimeException
                                    ("TicketType not found: " + item.ticketTypeId()));

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
                if(available < item.quantity()){
                    throw new RuntimeException("Not enough stock for TicketType " + item.ticketTypeId());
                }
                redisTemplate
                        .opsForHash()
                        .put(ticketTypeKey, "availableStock", available - item.quantity());
                items.add(new ReservationItem(item.ticketTypeId(), name, item.quantity(), price));
                totalPrice += item.quantity() * price;
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lock interrupted for ticket type: " + item.ticketTypeId(), e);
            }
            finally {
                lock.unlock();
            }
        }
        Reservation reservationObj = new Reservation(
                UUID.fromString(reservationId),
                reservation.attendeeId(),
                reservation.eventId(),
                items,
                totalPrice,
                LocalDateTime.now().plus(EXPIRATION_TIME)
        );
        String reservationKey = String.format("%s%s", RESERVATION_PREFIX, reservationId);
        redisTemplate.opsForValue().set(reservationKey, reservation, TTL);
        return reservationObj;
        // TODO: Publish Events to RabbitMQ
    }
}
