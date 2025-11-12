package com.ticketshall.attendance.mq.consumers;

import com.ticketshall.attendance.constants.GeneralConstants;
import com.ticketshall.attendance.entity.id.InboxMessageId;
import com.ticketshall.attendance.mq.events.TicketCreatedEvent;
import com.ticketshall.attendance.mq.events.TicketDeletedEvent;
import com.ticketshall.attendance.mq.events.TicketUpdatedEvent;
import com.ticketshall.attendance.mapper.TicketMapper;
import com.ticketshall.attendance.entity.Attendee;
import com.ticketshall.attendance.entity.Event;
import com.ticketshall.attendance.entity.InboxMessage;
import com.ticketshall.attendance.entity.Ticket;
import com.ticketshall.attendance.repository.AttendeeRepository;
import com.ticketshall.attendance.repository.EventRepository;
import com.ticketshall.attendance.repository.InboxRepository;
import com.ticketshall.attendance.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketConsumer {
    private final AttendeeRepository attendeeRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final InboxRepository inboxRepository;
    private final TicketMapper ticketMapper;

    @RabbitListener(queues = "${app.rabbitmq.queues.ticketCreated}")
    @Transactional
    public void handleTicketCreated(TicketCreatedEvent event) {
        InboxMessageId inboxMessageId = new InboxMessageId(event.id(), GeneralConstants.TICKET_CREATED_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        try {
            log.info("Processing TicketCreatedEvent: {}", event);

            Event ticketEvent = eventRepository.findById(event.eventId())
                    .orElseThrow(() -> new RuntimeException("Event not found with ID: " + event.eventId()));

            Attendee attendee = attendeeRepository.findById(event.userId())
                    .orElseThrow(() -> new RuntimeException("Attendee not found with ID: " + event.userId()));

            Ticket ticket = ticketMapper.toTicket(event);
            ticket.setAttendee(attendee);
            ticket.setEvent(ticketEvent);

            ticketRepository.save(ticket);

            saveInboxRecord(inboxMessageId);
            log.info("Successfully created ticket with ID: {}", event.id());
        } catch (Exception e) {
            log.error("Error processing TicketCreatedEvent: {}", event, e);
            throw e;
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.ticketUpdated}")
    @Transactional
    public void handleTicketUpdated(TicketUpdatedEvent event) {
        InboxMessageId inboxMessageId = new InboxMessageId(event.id(), GeneralConstants.TICKET_UPDATED_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        try {
            log.info("Processing TicketUpdatedEvent: {}", event);
            Event ticketEvent = eventRepository.findById(event.eventId())
                    .orElseThrow(() -> new RuntimeException("Event not found with ID: " + event.eventId()));

            Attendee attendee = attendeeRepository.findById(event.userId())
                    .orElseThrow(() -> new RuntimeException("Attendee not found with ID: " + event.userId()));

            Ticket ticket = ticketMapper.toTicket(event);
            ticket.setAttendee(attendee);
            ticket.setEvent(ticketEvent);

            ticketRepository.save(ticket);
            saveInboxRecord(inboxMessageId);
        }  catch (Exception e) {
            log.error("Error processing TicketUpdatedEvent: {}", event, e);
            throw e;
        }
    }
    @RabbitListener(queues = "${app.rabbitmq.queues.ticketDeleted}")
    public void handleTicketDeleted(TicketDeletedEvent event) {
        InboxMessageId inboxMessageId = new InboxMessageId(event.id(), GeneralConstants.TICKET_DELETED_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        try {
            log.info("Processing TicketDeletedEvent: {}", event);
            ticketRepository.deleteById(event.id());
            saveInboxRecord(inboxMessageId);
        } catch (Exception e) {
            log.error("Error processing TicketDeletedEvent: {}", event, e);
            throw e;
        }
    }
    private void saveInboxRecord(InboxMessageId id) {
        InboxMessage message = InboxMessage.builder()
                .inboxMessageId(id)
                .receivedAt(LocalDateTime.now())
                .build();
        inboxRepository.save(message);
    }
}
