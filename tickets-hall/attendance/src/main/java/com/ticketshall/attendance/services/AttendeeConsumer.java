package com.ticketshall.attendance.services;

import com.ticketshall.attendance.events.AttendeeCreatedEvent;
import com.ticketshall.attendance.events.AttendeeDeletedEvent;
import com.ticketshall.attendance.events.AttendeeUpdatedEvent;
import com.ticketshall.attendance.mapper.AttendeeMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.ticketshall.attendance.models.Attendee;
import com.ticketshall.attendance.repository.AttendeeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendeeConsumer {
    private final AttendeeRepository attendeeRepository;
    private final AttendeeMapper attendeeMapper;

    @RabbitListener(queues = "${app.rabbitmq.queues.userCreated}")
    public void handleUserCreated(AttendeeCreatedEvent event) {
        try {
            log.info("Processing UserCreatedEvent: {}", event);
            Attendee attendee = attendeeMapper.toAttendee(event);
            attendeeRepository.save(attendee);
            log.info("Successfully created attendee with ID: {}", event.id());
        } catch (Exception e) {
            log.error("Error processing UserCreatedEvent: {}", event, e);
            throw e;
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.userUpdated}")
    public void handleUserUpdated(AttendeeUpdatedEvent event) {
        try {
            log.info("Processing UserUpdatedEvent: {}", event);
            Attendee attendee = attendeeMapper.toAttendee(event);
            attendeeRepository.save(attendee);
        }  catch (Exception e) {
            log.error("Error processing UserUpdatedEvent: {}", event, e);
            throw e;
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.userDeleted}")
    public void handleUserDeleted(AttendeeDeletedEvent event) {
        try {
            log.info("Processing UserDeletedEvent: {}", event);
            attendeeRepository.deleteById(event.id());
        }  catch (Exception e) {
            log.error("Error processing UserDeletedEvent: {}", event, e);
            throw e;
        }
    }
}
