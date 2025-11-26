package com.ticketshall.tickets.mq.consumers;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentConsumerTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AttendeeRepository attendeeRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private ReservationService reservationService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RBucket<Reservation> rBucket;

    @InjectMocks
    private PaymentConsumer paymentConsumer;

    private UUID reservationId;
    private UUID eventId;
    private UUID ticketTypeId;
    private UUID attendeeId;
    private Reservation reservation;
    private TicketType ticketType;
    private Attendee attendee;
    private Event event;

    @BeforeEach
    void setUp() {
        reservationId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        ticketTypeId = UUID.randomUUID();
        attendeeId = UUID.randomUUID();

        ReservationItem item = new ReservationItem(ticketTypeId, "VIP", 1, 100.0f);
        reservation = new Reservation(reservationId, attendeeId, eventId, "paymentId", List.of(item), 100.0f, LocalDateTime.now());

        ticketType = new TicketType();
        ticketType.setId(ticketTypeId);
        ticketType.setAvailableStock(50);
        ticketType.setName("VIP");

        attendee = Attendee.builder().id(attendeeId).build();
        event = Event.builder().id(eventId).build();

        ReflectionTestUtils.setField(paymentConsumer, "paymentExchange", "payment.exchange");
        ReflectionTestUtils.setField(paymentConsumer, "paymentSucceededRoutingKey", "payment.succeeded");
    }

    @Test
    void handlePaymentSucceeded_ShouldCreateTicketsAndPublishEvent() {
        // Arrange
        PaymentSucceededEvent event = new PaymentSucceededEvent(reservationId.toString());

        doReturn(rBucket).when(redissonClient).<Reservation>getBucket(anyString());
        when(rBucket.get()).thenReturn(reservation);
        when(ticketTypeRepository.findById(ticketTypeId)).thenReturn(Optional.of(ticketType));
        when(attendeeRepository.getReferenceById(attendeeId)).thenReturn(attendee);
        when(eventRepository.getReferenceById(eventId)).thenReturn(this.event);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        // Act
        paymentConsumer.handlePaymentSucceeded(event);

        // Assert
        verify(ticketTypeRepository).save(ticketType); // Stock update
        verify(ticketRepository).save(any(Ticket.class)); // Ticket creation
        verify(reservationService).expireReservation(reservationId, false); // Expire without stock recovery
        verify(rabbitTemplate).convertAndSend(eq("payment.exchange"), eq("payment.succeeded"), any(TicketCreatedEvent.class));
    }

    @Test
    void handlePaymentFailed_ShouldExpireReservation() {
        // Arrange
        PaymentFailedEvent event = new PaymentFailedEvent(reservationId.toString());

        // Act
        paymentConsumer.handlePaymentFailed(event);

        // Assert
        verify(reservationService).expireReservation(reservationId, false);
    }
}
