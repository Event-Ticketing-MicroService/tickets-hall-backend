package com.ticketshall.tickets.mq.producers;

import com.ticketshall.tickets.mq.events.TicketCreatedEvent;
import com.ticketshall.tickets.mq.events.TicketDeletedEvent;
import com.ticketshall.tickets.mq.events.TicketUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketEventProducer {
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchanges.ticket}")
    private String ticketExchange;

    @Value("${app.rabbitmq.routing.ticketCreated}")
    private String ticketCreatedRouting;

    @Value("${app.rabbitmq.routing.ticketUpdated}")
    private String ticketUpdatedRouting;

    @Value("${app.rabbitmq.routing.ticketDeleted}")
    private String ticketDeletedRouting;

    public void publishTicketCreatedEvent(TicketCreatedEvent ticketCreatedEvent) {
        rabbitTemplate.convertAndSend(ticketExchange, ticketCreatedRouting, ticketCreatedEvent);
    }

    public void publishTicketUpdatedEvent(TicketUpdatedEvent ticketUpdatedEvent) {
        rabbitTemplate.convertAndSend(ticketExchange, ticketUpdatedRouting, ticketUpdatedEvent);
    }

    public void publishTicketDeletedEvent(TicketDeletedEvent ticketDeletedEvent) {
        rabbitTemplate.convertAndSend(ticketExchange, ticketDeletedRouting, ticketDeletedEvent);
    }
}
