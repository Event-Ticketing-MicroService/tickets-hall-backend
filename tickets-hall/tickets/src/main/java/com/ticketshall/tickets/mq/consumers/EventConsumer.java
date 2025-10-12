package com.ticketshall.tickets.mq.consumers;

import com.ticketshall.tickets.mapper.EventMapper;
import com.ticketshall.tickets.models.Event;
import com.ticketshall.tickets.mq.events.EventCreatedEvent;
import com.ticketshall.tickets.mq.events.EventDeletedEvent;
import com.ticketshall.tickets.mq.events.EventUpdatedEvent;
import com.ticketshall.tickets.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

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
            log.error("Error processing EventCreatedEvent: {}", event, e);
            throw e;
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.eventUpdated}")
    public void handleEventUpdated(EventUpdatedEvent event) {
        try {
            log.info("Processing EventUpdatedEvent: {}", event);

            Event updatedEvent = eventMapper.toEvent(event);

            eventRepository.save(updatedEvent);
        }  catch (Exception e) {
            log.error("Error processing EventUpdatedEvent: {}", event, e);
            throw e;
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.eventDeleted}")
    public void handleEventDeleted(EventDeletedEvent event) {
        try {
            log.info("Processing EventDeletedEvent: {}", event);
            eventRepository.deleteById(event.id());
        }  catch (Exception e) {
            log.error("Error processing EventDeletedEvent: {}", event, e);
            throw e;
        }
    }
}
