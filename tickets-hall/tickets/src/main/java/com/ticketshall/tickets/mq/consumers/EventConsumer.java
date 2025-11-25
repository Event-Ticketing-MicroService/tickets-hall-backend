package com.ticketshall.tickets.mq.consumers;

import com.ticketshall.tickets.constants.GeneralConstants;
import com.ticketshall.tickets.mapper.EventMapper;
import com.ticketshall.tickets.models.Event;
import com.ticketshall.tickets.models.InboxMessage;
import com.ticketshall.tickets.models.id.InboxMessageId;
import com.ticketshall.tickets.mq.events.EventCreatedEvent;
import com.ticketshall.tickets.mq.events.EventDeletedEvent;
import com.ticketshall.tickets.mq.events.EventUpdatedEvent;
import com.ticketshall.tickets.repository.EventRepository;
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
public class EventConsumer {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final InboxRepository inboxRepository;

    @RabbitListener(queues = "${app.rabbitmq.queues.eventCreated}")
    @Transactional
    public void handleEventCreated(EventCreatedEvent event) {
        InboxMessageId inboxMessageId = new InboxMessageId(event.id(), GeneralConstants.Event_Created_INBOX_TYPE);
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
        InboxMessageId inboxMessageId = new InboxMessageId(event.id(), GeneralConstants.Event_Deleted_INBOX_TYPE);
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
