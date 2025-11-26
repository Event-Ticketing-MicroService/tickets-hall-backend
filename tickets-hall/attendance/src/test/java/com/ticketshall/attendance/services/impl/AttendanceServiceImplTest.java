package com.ticketshall.attendance.services.impl;

import com.ticketshall.attendance.entity.Attendee;
import com.ticketshall.attendance.entity.Event;
import com.ticketshall.attendance.entity.Ticket;
import com.ticketshall.attendance.error.exceptions.EventAlreadyEndedException;
import com.ticketshall.attendance.error.exceptions.TicketAlreadyUsedException;
import com.ticketshall.attendance.error.exceptions.TicketNotFoundException;
import com.ticketshall.attendance.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Ticket validTicket;
    private Event futureEvent;
    private Event pastEvent;
    private Attendee attendee;

    @BeforeEach
    void setUp() {
        // Create a future event (not ended)
        futureEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Future Event")
                .description("Test Event")
                .location("Test Location")
                .startsAtUtc(LocalDateTime.now().plusDays(1))
                .endsAtUtc(LocalDateTime.now().plusDays(2))
                .build();

        // Create a past event (already ended)
        pastEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Past Event")
                .description("Test Event")
                .location("Test Location")
                .startsAtUtc(LocalDateTime.now().minusDays(2))
                .endsAtUtc(LocalDateTime.now().minusDays(1))
                .build();

        // Create an attendee
        attendee = Attendee.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // Create a valid ticket
        validTicket = Ticket.builder()
                .id(UUID.randomUUID())
                .code("VALID-TICKET-123")
                .usedAtUtc(null)
                .attendee(attendee)
                .event(futureEvent)
                .build();
    }

    @Test
    void useTicket_WithValidTicket_ShouldMarkTicketAsUsed() {
        // Arrange
        when(ticketRepository.findByCode("VALID-TICKET-123"))
                .thenReturn(Optional.of(validTicket));
        when(ticketRepository.save(any(Ticket.class)))
                .thenReturn(validTicket);

        // Act
        attendanceService.useTicket("VALID-TICKET-123");

        // Assert
        assertNotNull(validTicket.getUsedAtUtc());
        verify(ticketRepository, times(1)).findByCode("VALID-TICKET-123");
        verify(ticketRepository, times(1)).save(validTicket);
    }

    @Test
    void useTicket_WithNonExistentTicket_ShouldThrowTicketNotFoundException() {
        // Arrange
        when(ticketRepository.findByCode("NON-EXISTENT"))
                .thenReturn(Optional.empty());

        // Act & Assert
        TicketNotFoundException exception = assertThrows(
                TicketNotFoundException.class,
                () -> attendanceService.useTicket("NON-EXISTENT")
        );

        assertEquals("Ticket with code 'NON-EXISTENT' not found", exception.getMessage());
        verify(ticketRepository, times(1)).findByCode("NON-EXISTENT");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void useTicket_WithAlreadyUsedTicket_ShouldThrowTicketAlreadyUsedException() {
        // Arrange
        validTicket.setUsedAtUtc(LocalDateTime.now().minusHours(1));
        when(ticketRepository.findByCode("VALID-TICKET-123"))
                .thenReturn(Optional.of(validTicket));

        // Act & Assert
        TicketAlreadyUsedException exception = assertThrows(
                TicketAlreadyUsedException.class,
                () -> attendanceService.useTicket("VALID-TICKET-123")
        );

        assertEquals("Ticket with code 'VALID-TICKET-123' has already been used", exception.getMessage());
        verify(ticketRepository, times(1)).findByCode("VALID-TICKET-123");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void useTicket_WithEndedEvent_ShouldThrowEventAlreadyEndedException() {
        // Arrange
        Ticket ticketForPastEvent = Ticket.builder()
                .id(UUID.randomUUID())
                .code("PAST-EVENT-TICKET")
                .usedAtUtc(null)
                .attendee(attendee)
                .event(pastEvent)
                .build();

        when(ticketRepository.findByCode("PAST-EVENT-TICKET"))
                .thenReturn(Optional.of(ticketForPastEvent));

        // Act & Assert
        EventAlreadyEndedException exception = assertThrows(
                EventAlreadyEndedException.class,
                () -> attendanceService.useTicket("PAST-EVENT-TICKET")
        );

        assertEquals("Event has already ended", exception.getMessage());
        verify(ticketRepository, times(1)).findByCode("PAST-EVENT-TICKET");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void useTicket_WithEventEndingNow_ShouldThrowEventAlreadyEndedException() {
        // Arrange
        Event eventEndingNow = Event.builder()
                .id(UUID.randomUUID())
                .title("Event Ending Now")
                .description("Test Event")
                .location("Test Location")
                .startsAtUtc(LocalDateTime.now().minusHours(2))
                .endsAtUtc(LocalDateTime.now().minusSeconds(1))
                .build();

        Ticket ticketForEndingEvent = Ticket.builder()
                .id(UUID.randomUUID())
                .code("ENDING-EVENT-TICKET")
                .usedAtUtc(null)
                .attendee(attendee)
                .event(eventEndingNow)
                .build();

        when(ticketRepository.findByCode("ENDING-EVENT-TICKET"))
                .thenReturn(Optional.of(ticketForEndingEvent));

        // Act & Assert
        EventAlreadyEndedException exception = assertThrows(
                EventAlreadyEndedException.class,
                () -> attendanceService.useTicket("ENDING-EVENT-TICKET")
        );

        assertEquals("Event has already ended", exception.getMessage());
        verify(ticketRepository, times(1)).findByCode("ENDING-EVENT-TICKET");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void useTicket_WithEventJustStarted_ShouldSucceed() {
        // Arrange
        Event ongoingEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Ongoing Event")
                .description("Test Event")
                .location("Test Location")
                .startsAtUtc(LocalDateTime.now().minusMinutes(30))
                .endsAtUtc(LocalDateTime.now().plusHours(2))
                .build();

        Ticket ticketForOngoingEvent = Ticket.builder()
                .id(UUID.randomUUID())
                .code("ONGOING-EVENT-TICKET")
                .usedAtUtc(null)
                .attendee(attendee)
                .event(ongoingEvent)
                .build();

        when(ticketRepository.findByCode("ONGOING-EVENT-TICKET"))
                .thenReturn(Optional.of(ticketForOngoingEvent));
        when(ticketRepository.save(any(Ticket.class)))
                .thenReturn(ticketForOngoingEvent);

        // Act
        attendanceService.useTicket("ONGOING-EVENT-TICKET");

        // Assert
        assertNotNull(ticketForOngoingEvent.getUsedAtUtc());
        verify(ticketRepository, times(1)).findByCode("ONGOING-EVENT-TICKET");
        verify(ticketRepository, times(1)).save(ticketForOngoingEvent);
    }
}
