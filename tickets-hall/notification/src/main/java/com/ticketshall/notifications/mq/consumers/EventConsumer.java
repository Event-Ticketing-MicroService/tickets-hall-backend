package com.ticketshall.notifications.mq.consumers;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.ticketshall.notifications.entity.Event;
import com.ticketshall.notifications.mapper.EventMapper;
import com.ticketshall.notifications.mq.events.EventCreatedEvent;
import com.ticketshall.notifications.mq.events.EventDeletedEvent;
import com.ticketshall.notifications.mq.events.EventUpdatedEvent;
import com.ticketshall.notifications.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventConsumer {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @RabbitListener(queues = "${app.rabbitmq.queues.eventCreated}")
    public void handleEventCreated(EventCreatedEvent event) {
            Event createdEvent = eventMapper.toEvent(event);

            eventRepository.save(createdEvent);
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.eventUpdated}")
    public void handleEventUpdated(EventUpdatedEvent event) {
        Event updatedEvent = eventMapper.toEvent(event);

        eventRepository.save(updatedEvent);

    }

    @RabbitListener(queues = "${app.rabbitmq.queues.eventDeleted}")
    public void handleEventDeleted(EventDeletedEvent event) {
        eventRepository.deleteById(event.id());
    }
}
