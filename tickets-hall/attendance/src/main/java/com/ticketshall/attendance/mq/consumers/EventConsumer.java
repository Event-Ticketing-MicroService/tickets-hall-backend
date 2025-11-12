package com.ticketshall.attendance.mq.consumers;

import com.ticketshall.attendance.constants.GeneralConstants;
import com.ticketshall.attendance.entity.id.InboxMessageId;
import com.ticketshall.attendance.mq.events.EventCreatedEvent;
import com.ticketshall.attendance.mq.events.EventDeletedEvent;
import com.ticketshall.attendance.mq.events.EventUpdatedEvent;
import com.ticketshall.attendance.mapper.EventMapper;
import com.ticketshall.attendance.entity.Event;
import com.ticketshall.attendance.entity.InboxMessage;
import com.ticketshall.attendance.repository.EventRepository;
import com.ticketshall.attendance.repository.InboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {
    private final EventRepository eventRepository;
    private final InboxRepository inboxRepository;
    private final EventMapper eventMapper;

    @RabbitListener(queues = "${app.rabbitmq.queues.eventCreated}")
    @Transactional
    public void handleEventCreated(EventCreatedEvent event) {
        InboxMessageId  inboxMessageId = new InboxMessageId(event.id(), GeneralConstants.Event_Created_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        try {
            log.info("Processing EventCreatedEvent: {}", event);
            log.debug("Event details - ID: {}, Title: {}, Location: {}, Start: {}, End: {}",
                    event.id(), event.title(), event.location(), event.startsAtUtc(), event.endsAtUtc());

            Event createdEvent = eventMapper.toEvent(event);

            eventRepository.save(createdEvent);
            saveInboxRecord(inboxMessageId);
            log.info("Successfully created event with ID: {}", event.id());
        } catch (Exception e) {
            log.error("Error processing EventCreatedEvent: {}", event, e);
            throw e;
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.eventUpdated}")
    @Transactional
    public void handleEventUpdated(EventUpdatedEvent event) {
        InboxMessageId inboxMessageId = new  InboxMessageId(event.id(), GeneralConstants.Event_Updated_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        try {
            log.info("Processing EventUpdatedEvent: {}", event);

            Event updatedEvent = eventMapper.toEvent(event);

            eventRepository.save(updatedEvent);
            saveInboxRecord(inboxMessageId);
        }  catch (Exception e) {
            log.error("Error processing EventUpdatedEvent: {}", event, e);
            throw e;
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.eventDeleted}")
    @Transactional
    public void handleEventDeleted(EventDeletedEvent event) {
        InboxMessageId inboxMessageId  = new InboxMessageId(event.id(), GeneralConstants.Event_Deleted_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        try {
            log.info("Processing EventDeletedEvent: {}", event);
            eventRepository.deleteById(event.id());
            saveInboxRecord(inboxMessageId);
        }  catch (Exception e) {
            log.error("Error processing EventDeletedEvent: {}", event, e);
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
