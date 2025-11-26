package com.ticketshall.notifications.mq.consumers;

import com.google.zxing.WriterException;
import com.ticketshall.notifications.constants.GeneralConstants;
import com.ticketshall.notifications.entity.Event;
import com.ticketshall.notifications.entity.InboxMessage;
import com.ticketshall.notifications.entity.User;
import com.ticketshall.notifications.entity.id.InboxMessageId;
import com.ticketshall.notifications.mq.events.TicketCreatedEvent;
import com.ticketshall.notifications.repository.EventRepository;
import com.ticketshall.notifications.repository.InboxRepository;
import com.ticketshall.notifications.repository.UserRepository;
import com.ticketshall.notifications.service.EmailService;
import com.ticketshall.notifications.service.QrCodeService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketConsumerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private QrCodeService qrCodeService;

    @Mock
    private InboxRepository inboxRepository;

    @InjectMocks
    private TicketConsumer ticketConsumer;

    private User testUser;
    private Event testEvent;
    private TicketCreatedEvent ticketCreatedEvent;
    private UUID ticketId;
    private UUID userId;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        userId = UUID.randomUUID();
        eventId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        testEvent = Event.builder()
                .id(eventId)
                .title("Rock Concert")
                .location("Madison Square Garden")
                .startsAtUtc(LocalDateTime.now().plusDays(7))
                .endsAtUtc(LocalDateTime.now().plusDays(7).plusHours(3))
                .build();

        ticketCreatedEvent = new TicketCreatedEvent(
                ticketId,
                "TICKET-ABC123",
                userId,
                eventId
        );
    }

    @Test
    void handleTicketCreated_WithValidEvent_ShouldSendEmail() throws WriterException, IOException, MessagingException {
        // Arrange
        when(inboxRepository.existsById(any(InboxMessageId.class))).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(qrCodeService.generateQrCodeBase64("TICKET-ABC123", 150, 150))
                .thenReturn("data:image/png;base64,mockQrCode");
        doNothing().when(emailService).sendTemplate(anyString(), anyString(), anyString(), anyMap());

        // Act
        ticketConsumer.handleTicketCreated(ticketCreatedEvent);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(eventRepository, times(1)).findById(eventId);
        verify(qrCodeService, times(1)).generateQrCodeBase64("TICKET-ABC123", 150, 150);
        verify(emailService, times(1)).sendTemplate(
                eq("ticket-created"),
                eq("john.doe@example.com"),
                eq("Ticket for Rock Concert"),
                anyMap()
        );
        verify(inboxRepository, times(1)).save(any(InboxMessage.class));
    }

    @Test
    void handleTicketCreated_ShouldPassCorrectVariablesToEmailService() throws WriterException, IOException, MessagingException {
        // Arrange
        when(inboxRepository.existsById(any(InboxMessageId.class))).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(qrCodeService.generateQrCodeBase64(anyString(), anyInt(), anyInt()))
                .thenReturn("data:image/png;base64,mockQrCode");

        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);

        // Act
        ticketConsumer.handleTicketCreated(ticketCreatedEvent);

        // Assert
        verify(emailService).sendTemplate(
                eq("ticket-created"),
                eq("john.doe@example.com"),
                eq("Ticket for Rock Concert"),
                variablesCaptor.capture()
        );

        Map<String, Object> capturedVariables = variablesCaptor.getValue();
        assertEquals("John", capturedVariables.get("name"));
        assertEquals("Rock Concert", capturedVariables.get("eventName"));
        assertEquals(testEvent.getStartsAtUtc(), capturedVariables.get("startTime"));
        assertEquals(testEvent.getEndsAtUtc(), capturedVariables.get("endTime"));
        assertEquals("Madison Square Garden", capturedVariables.get("location"));
        assertEquals("data:image/png;base64,mockQrCode", capturedVariables.get("qrCode"));
    }

    @Test
    void handleTicketCreated_WhenMessageAlreadyProcessed_ShouldNotSendEmail() throws WriterException, IOException, MessagingException {
        // Arrange
        when(inboxRepository.existsById(any(InboxMessageId.class))).thenReturn(true);

        // Act
        ticketConsumer.handleTicketCreated(ticketCreatedEvent);

        // Assert
        verify(inboxRepository, times(1)).existsById(any(InboxMessageId.class));
        verify(userRepository, never()).findById(any());
        verify(eventRepository, never()).findById(any());
        verify(qrCodeService, never()).generateQrCodeBase64(anyString(), anyInt(), anyInt());
        verify(emailService, never()).sendTemplate(anyString(), anyString(), anyString(), anyMap());
        verify(inboxRepository, never()).save(any(InboxMessage.class));
    }

    @Test
    void handleTicketCreated_ShouldSaveInboxRecord() throws WriterException, IOException, MessagingException {
        // Arrange
        when(inboxRepository.existsById(any(InboxMessageId.class))).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(qrCodeService.generateQrCodeBase64(anyString(), anyInt(), anyInt()))
                .thenReturn("data:image/png;base64,mockQrCode");

        ArgumentCaptor<InboxMessage> inboxMessageCaptor = ArgumentCaptor.forClass(InboxMessage.class);

        // Act
        ticketConsumer.handleTicketCreated(ticketCreatedEvent);

        // Assert
        verify(inboxRepository, times(1)).save(inboxMessageCaptor.capture());

        InboxMessage savedMessage = inboxMessageCaptor.getValue();
        assertNotNull(savedMessage);
        assertNotNull(savedMessage.getInboxMessageId());
        assertEquals(ticketId, savedMessage.getInboxMessageId().getId());
        assertEquals(GeneralConstants.TICKET_CREATED_INBOX_TYPE, savedMessage.getInboxMessageId().getType());
        assertNotNull(savedMessage.getReceivedAt());
    }

    @Test
    void handleTicketCreated_WhenUserNotFound_ShouldThrowException() throws MessagingException {
        // Arrange
        when(inboxRepository.existsById(any(InboxMessageId.class))).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> {
            ticketConsumer.handleTicketCreated(ticketCreatedEvent);
        });

        verify(emailService, never()).sendTemplate(anyString(), anyString(), anyString(), anyMap());
        verify(inboxRepository, never()).save(any(InboxMessage.class));
    }

    @Test
    void handleTicketCreated_WhenEventNotFound_ShouldThrowException() throws MessagingException {
        // Arrange
        when(inboxRepository.existsById(any(InboxMessageId.class))).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> {
            ticketConsumer.handleTicketCreated(ticketCreatedEvent);
        });

        verify(emailService, never()).sendTemplate(anyString(), anyString(), anyString(), anyMap());
        verify(inboxRepository, never()).save(any(InboxMessage.class));
    }

    @Test
    void handleTicketCreated_ShouldGenerateQrCodeWithCorrectParameters() throws WriterException, IOException, MessagingException {
        // Arrange
        when(inboxRepository.existsById(any(InboxMessageId.class))).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(qrCodeService.generateQrCodeBase64(anyString(), anyInt(), anyInt()))
                .thenReturn("data:image/png;base64,mockQrCode");

        // Act
        ticketConsumer.handleTicketCreated(ticketCreatedEvent);

        // Assert
        verify(qrCodeService, times(1)).generateQrCodeBase64("TICKET-ABC123", 150, 150);
    }
}
