package com.ticketshall.events.rabbitmq.producers;

import com.ticketshall.events.dtos.messages.EventDeletedMessage;
import com.ticketshall.events.dtos.messages.EventUpsertedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventProducer {
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchanges.event}")
    private String eventExchangeName;

    @Value("${app.rabbitmq.routing.event-created}")
    private String eventCreatedRoutingKey;

    @Value("${app.rabbitmq.routing.event-updated}")
    private String eventUpdatedRoutingKey;

    @Value("${app.rabbitmq.routing.event-deleted}")
    private String eventDeletedRoutingKey;

    public EventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendEventCreated(EventUpsertedMessage message) {
        log.info("Publishing Event created Message");
        rabbitTemplate.convertAndSend(eventExchangeName, eventCreatedRoutingKey, message);
    }

    public void sendEventUpdated(EventUpsertedMessage message) {
        log.info("Publishing Event Updated Message");
        rabbitTemplate.convertAndSend(eventExchangeName, eventUpdatedRoutingKey, message);
    }

    public void sendEventDeleted(EventDeletedMessage message) {
        log.info("Event Deleted Message");
        rabbitTemplate.convertAndSend(eventExchangeName, eventDeletedRoutingKey, message);
    }
}