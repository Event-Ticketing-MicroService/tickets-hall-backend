package com.ticketshall.tickets.mq.consumers;

import com.ticketshall.tickets.constants.GeneralConstants;
import com.ticketshall.tickets.mapper.AttendeeMapper;
import com.ticketshall.tickets.models.Attendee;
import com.ticketshall.tickets.models.InboxMessage;
import com.ticketshall.tickets.models.id.InboxMessageId;
import com.ticketshall.tickets.mq.events.AttendeeCreatedEvent;
import com.ticketshall.tickets.mq.events.AttendeeDeletedEvent;
import com.ticketshall.tickets.mq.events.AttendeeUpdatedEvent;
import com.ticketshall.tickets.repository.AttendeeRepository;
import com.ticketshall.tickets.repository.InboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendeeConsumer {
    private final AttendeeRepository attendeeRepository;
    private final AttendeeMapper attendeeMapper;
    private final InboxRepository inboxRepository;

    @RabbitListener(queues = "${app.rabbitmq.queues.userCreated}")
    @Transactional
    public void handleUserCreated(AttendeeCreatedEvent event) {
        InboxMessageId inboxMessageId = new InboxMessageId(event.id(), GeneralConstants.ATTENDEE_CREATED_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        try {
            log.info("Processing UserCreatedEvent: {}", event);
            Attendee attendee = attendeeMapper.toAttendee(event);
            attendeeRepository.save(attendee);
            saveInboxRecord(inboxMessageId);
            log.info("Successfully created attendee with ID: {}", event.id());
        } catch (Exception e) {
            log.error("Error processing UserCreatedEvent: {}", event, e);
            throw e;
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.userUpdated}")
    @Transactional
    public void handleUserUpdated(AttendeeUpdatedEvent event) {
        InboxMessageId inboxMessageId = new InboxMessageId(event.id(), GeneralConstants.ATTENDEE_UPDATED_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        try {
            log.info("Processing UserUpdatedEvent: {}", event);
            Attendee attendee = attendeeMapper.toAttendee(event);
            attendeeRepository.save(attendee);
            saveInboxRecord(inboxMessageId);
        }  catch (Exception e) {
            log.error("Error processing UserUpdatedEvent: {}", event, e);
            throw e;
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.userDeleted}")
    @Transactional
    public void handleUserDeleted(AttendeeDeletedEvent event) {
        InboxMessageId inboxMessageId = new InboxMessageId(event.id(), GeneralConstants.ATTENDEE_DELETED_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        try {
            log.info("Processing UserDeletedEvent: {}", event);
            attendeeRepository.deleteById(event.id());
            saveInboxRecord(inboxMessageId);
        }  catch (Exception e) {
            log.error("Error processing UserDeletedEvent: {}", event, e);
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

