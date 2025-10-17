package com.ticketshall.attendance.services;

import com.ticketshall.attendance.events.TicketCreatedEvent;
import com.ticketshall.attendance.events.TicketDeletedEvent;
import com.ticketshall.attendance.events.TicketUpdatedEvent;
import com.ticketshall.attendance.mapper.TicketMapper;
import com.ticketshall.attendance.models.Attendee;
import com.ticketshall.attendance.models.Event;
import com.ticketshall.attendance.models.Ticket;
import com.ticketshall.attendance.repository.AttendeeRepository;
import com.ticketshall.attendance.repository.EventRepository;
import com.ticketshall.attendance.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketConsumer {
    private final AttendeeRepository attendeeRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    @RabbitListener(queues = "${app.rabbitmq.queues.ticketCreated}")
    public void handleTicketCreated(TicketCreatedEvent event) {
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
            log.info("Successfully created ticket with ID: {}", event.id());
        } catch (Exception e) {
            log.error("Error processing TicketCreatedEvent: {}", event, e);
            throw e;
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.ticketUpdated}")
    public void handleTicketUpdated(TicketUpdatedEvent event) {
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
        }  catch (Exception e) {
            log.error("Error processing TicketUpdatedEvent: {}", event, e);
            throw e;
        }
    }
    @RabbitListener(queues = "${app.rabbitmq.queues.ticketDeleted}")
    public void handleTicketDeleted(TicketDeletedEvent event) {
        try {
            log.info("Processing TicketDeletedEvent: {}", event);
            ticketRepository.deleteById(event.id());
        } catch (Exception e) {
            log.error("Error processing TicketDeletedEvent: {}", event, e);
            throw e;
        }
    }
}
