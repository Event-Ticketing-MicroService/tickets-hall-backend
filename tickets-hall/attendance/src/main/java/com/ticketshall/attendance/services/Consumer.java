package com.ticketshall.attendance.services;

import com.ticketshall.attendance.events.AttendeeCreatedEvent;
import com.ticketshall.attendance.mapper.AttendeeMapper;
import com.ticketshall.attendance.mapper.EventMapper;
import com.ticketshall.attendance.mapper.TicketMapper;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.ticketshall.attendance.events.EventCreatedEvent;
import com.ticketshall.attendance.events.TicketCreatedEvent;
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
    private final AttendeeMapper attendeeMapper;
    private final EventMapper eventMapper;
    private final TicketMapper ticketMapper;

    @RabbitListener(queues = "${app.rabbitmq.queues.userCreated}")
    public void handleUserCreated(AttendeeCreatedEvent event) {
        try {
            log.info("Processing UserCreatedEvent: {}", event);
            Attendee attendee = attendeeMapper.toAttendee(event);
            attendeeRepository.save(attendee);
            log.info("Successfully created attendee with ID: {}", event.id());
        } catch (Exception e) {
            log.error("Error processing UserCreatedEvent: {}", event, e.getMessage());
            throw e;
        }
    }


    @RabbitListener(queues = "${app.rabbitmq.queues.eventCreated}")
    public void handleEventCreated(EventCreatedEvent event) {
        try {
            log.info("Processing EventCreatedEvent: {}", event);
            log.debug("Event details - ID: {}, Title: {}, Location: {}, Start: {}, End: {}", 
                     event.id(), event.title(), event.location(), event.startsAtUtc(), event.endsAtUtc());
            
            Event createdEvent = eventMapper.toEvent(event);
            
            eventRepository.save(createdEvent);
            log.info("Successfully created event with ID: {}", event.id());
        } catch (Exception e) {
            log.error("Error processing EventCreatedEvent: {}", event, e.getMessage());
            throw e;
        }
    }

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
            log.error("Error processing TicketCreatedEvent: {}", event, e.getMessage());
            throw e;
        }
    }
}
