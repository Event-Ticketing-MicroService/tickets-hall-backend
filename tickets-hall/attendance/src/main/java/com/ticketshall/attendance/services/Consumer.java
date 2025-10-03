package com.ticketshall.attendance.services;

import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.ticketshall.attendance.events.EventCreatedEvent;
import com.ticketshall.attendance.events.TicketCreatedEvent;
import com.ticketshall.attendance.events.UserCreatedEvent;
import com.ticketshall.attendance.models.Attendee;
import com.ticketshall.attendance.models.Event;
import com.ticketshall.attendance.models.Ticket;
import com.ticketshall.attendance.repository.AttendeeRepository;
import com.ticketshall.attendance.repository.EventRepository;
import com.ticketshall.attendance.repository.TicketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class Consumer {
    private final AttendeeRepository attendeeRepository;
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    @RabbitListener(queues = "${app.rabbitmq.queues.userCreated}")
    @Transactional
    public void handleUserCreated(UserCreatedEvent event) {
        try {
            log.info("Processing UserCreatedEvent: {}", event);
            Attendee attendee = new Attendee(event.id(), event.firstName(), event.lastName(), event.email());
            attendeeRepository.save(attendee);
            log.info("Successfully created attendee with ID: {}", event.id());
        } catch (Exception e) {
            log.error("Error processing UserCreatedEvent: {}", event, e.getMessage());
            throw e;
        }
    }


    @RabbitListener(queues = "${app.rabbitmq.queues.eventCreated}")
    @Transactional
    public void handleEventCreated(EventCreatedEvent event) {
        try {
            log.info("Processing EventCreatedEvent: {}", event);
            log.debug("Event details - ID: {}, Title: {}, Location: {}, Start: {}, End: {}", 
                     event.id(), event.title(), event.location(), event.startsAtUTc(), event.endsAtUtc());
            
            Event createdEvent = new Event(event.id(), event.title(), event.description(), event.location(), event.startsAtUTc(), event.endsAtUtc());
            
            eventRepository.save(createdEvent);
            log.info("Successfully created event with ID: {}", event.id());
        } catch (Exception e) {
            log.error("Error processing EventCreatedEvent: {}", event, e.getMessage());
            throw e;
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.ticketCreated}")
    @Transactional
    public void handleTicketCreated(TicketCreatedEvent event) {
        try {
            log.info("Processing TicketCreatedEvent: {}", event);
            
            Event ticketEvent = eventRepository.findById(event.eventId())
                    .orElseThrow(() -> new RuntimeException("Event not found with ID: " + event.eventId()));
            
            Attendee attendee = attendeeRepository.findById(event.userId())
                    .orElseThrow(() -> new RuntimeException("Attendee not found with ID: " + event.userId()));

            Ticket ticket = Ticket
                .builder()
                .id(event.id())
                .attendee(attendee)
                .event(ticketEvent)
                .code(event.code())
                .build();

            ticketRepository.save(ticket);
            log.info("Successfully created ticket with ID: {}", event.id());
        } catch (Exception e) {
            log.error("Error processing TicketCreatedEvent: {}", event, e.getMessage());
            throw e;
        }
    }
}
