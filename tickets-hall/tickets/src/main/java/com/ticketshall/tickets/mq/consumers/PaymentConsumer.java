package com.ticketshall.tickets.mq.consumers;

import com.ticketshall.tickets.exceptions.TicketTypeNotFoundException;
import com.ticketshall.tickets.models.Attendee;
import com.ticketshall.tickets.models.Event;
import com.ticketshall.tickets.models.Ticket;
import com.ticketshall.tickets.models.TicketType;
import com.ticketshall.tickets.models.nonStoredModels.Reservation;
import com.ticketshall.tickets.models.nonStoredModels.ReservationItem;
import com.ticketshall.tickets.models.nonStoredModels.constants.GeneralConstants;
import com.ticketshall.tickets.mq.events.PaymentFailedEvent;
import com.ticketshall.tickets.mq.events.PaymentSucceededEvent;
import com.ticketshall.tickets.mq.events.TicketCreatedEvent;
import com.ticketshall.tickets.repository.AttendeeRepository;
import com.ticketshall.tickets.repository.EventRepository;
import com.ticketshall.tickets.repository.TicketRepository;
import com.ticketshall.tickets.repository.TicketTypeRepository;
import com.ticketshall.tickets.service.ReservationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentConsumer {
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final AttendeeRepository attendeeRepository;
    private final EventRepository eventRepository;
    private final RedissonClient redissonClient;
    private final ReservationService reservationService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchanges.payment}")
    private String paymentExchange;
    @Value("${app.rabbitmq.routing.paymentSucceeded}")
    private String paymentSucceededRoutingKey;
    @Value("${app.rabbitmq.routing.paymentFailed}")
    private String paymentFailedRoutingKey;

    @RabbitListener(queues = "${app.rabbitmq.queues.paymentSucceeded}")
    @Transactional
    public void handlePaymentSucceeded(PaymentSucceededEvent paymentSucceededEvent) {
        String redisKey = String.format("%s%s", GeneralConstants.REDIS_RESERVATION_PREFIX, paymentSucceededEvent.reservationId());
        RBucket<Reservation> bucket = redissonClient.getBucket(redisKey);
        Reservation reservation = bucket.get();
        List<TicketCreatedEvent> ticketCreatedEvents = new ArrayList<>();
        for (ReservationItem item : reservation.getItems()) {
            TicketType ticketType = ticketTypeRepository.findById(item.getTicketTypeId()).orElseThrow(() -> new TicketTypeNotFoundException("Ticket type not found"));
            ticketType.setAvailableStock(ticketType.getAvailableStock() - item.getQuantity());
            ticketTypeRepository.save(ticketType);
            Attendee attendee = attendeeRepository.getReferenceById(reservation.getAttendeeId());
            Event event = eventRepository.getReferenceById(reservation.getEventId());
            Ticket ticket = Ticket
                    .builder()
                    .attendee(attendee)
                    .event(event)
                    .code(UUID.randomUUID().toString())
                    .ticketType(ticketType)
                    .build();
            ticket = ticketRepository.save(ticket);
            ticketCreatedEvents.add(new TicketCreatedEvent(ticket.getId(), ticket.getCode(), ticket.getAttendee().getId(), ticket.getEvent().getId(), ticket.getTicketType().getId()));
        }
        reservationService.expireReservation(UUID.fromString(paymentSucceededEvent.reservationId()), false);
        for (TicketCreatedEvent event : ticketCreatedEvents) {
            rabbitTemplate.convertAndSend(paymentExchange, paymentSucceededRoutingKey, event);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.paymentFailed}")
    public void handlePaymentFailed(PaymentFailedEvent paymentFailedEvent) {
        reservationService.expireReservation(UUID.fromString(paymentFailedEvent.reservationId()), false);
    }
}
