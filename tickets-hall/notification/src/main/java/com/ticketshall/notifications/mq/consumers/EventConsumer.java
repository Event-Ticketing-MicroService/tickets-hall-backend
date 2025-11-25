package com.ticketshall.notifications.mq.consumers;

import com.ticketshall.notifications.constants.GeneralConstants;
import com.ticketshall.notifications.entity.InboxMessage;
import com.ticketshall.notifications.entity.id.InboxMessageId;
import com.ticketshall.notifications.repository.InboxRepository;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.ticketshall.notifications.entity.Event;
import com.ticketshall.notifications.mapper.EventMapper;
import com.ticketshall.notifications.mq.events.EventCreatedEvent;
import com.ticketshall.notifications.mq.events.EventDeletedEvent;
import com.ticketshall.notifications.mq.events.EventUpdatedEvent;
import com.ticketshall.notifications.repository.EventRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventConsumer {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final InboxRepository inboxRepository;

    @RabbitListener(queues = "${app.rabbitmq.queues.eventCreated}")
    @Transactional
    public void handleEventCreated(EventCreatedEvent event) {
        InboxMessageId inboxMessageId = new InboxMessageId(event.id(), GeneralConstants.EVENT_CREATED_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
            Event createdEvent = eventMapper.toEvent(event);

            eventRepository.save(createdEvent);

        saveInboxRecord(inboxMessageId);
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.eventUpdated}")
    @Transactional
    public void handleEventUpdated(EventUpdatedEvent event) {
        InboxMessageId inboxMessageId = new InboxMessageId(event.id(), GeneralConstants.EVENT_UPDATED_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        Event updatedEvent = eventMapper.toEvent(event);

        eventRepository.save(updatedEvent);

        saveInboxRecord(inboxMessageId);
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.eventDeleted}")
    @Transactional
    public void handleEventDeleted(EventDeletedEvent event) {
        InboxMessageId inboxMessageId = new InboxMessageId(event.id(), GeneralConstants.EVENT_DELETED_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        eventRepository.deleteById(event.id());
        saveInboxRecord(inboxMessageId);
    }

    private void saveInboxRecord(InboxMessageId id) {
        InboxMessage message = InboxMessage.builder()
                .inboxMessageId(id)
                .receivedAt(LocalDateTime.now())
                .build();
        inboxRepository.save(message);
    }
}
