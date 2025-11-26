package com.ticketshall.venues.mq.producers;

import com.ticketshall.venues.mq.events.WorkerCreatedMessage;
import com.ticketshall.venues.mq.events.WorkerDeletedMessage;
import com.ticketshall.venues.mq.events.WorkerUpdatedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueWorkerProducer {
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchanges.worker}")
    private String workerExchangeName;

    @Value("${app.rabbitmq.routing.workerCreated}")
    private String workerCreatedRoutingKey;

    @Value("${app.rabbitmq.routing.workerUpdated}")
    private String workerUpdatedRoutingKey;

    @Value("${app.rabbitmq.routing.workerDeleted}")
    private String workerDeletedRoutingKey;

    public void sendWorkerCreated(WorkerCreatedMessage message) {
        rabbitTemplate.convertAndSend(workerExchangeName, workerCreatedRoutingKey, message);
    }

    public void sendWorkerUpdated(WorkerUpdatedMessage message) {
        rabbitTemplate.convertAndSend(workerExchangeName, workerUpdatedRoutingKey, message);
    }

    public void sendWorkerDeleted(WorkerDeletedMessage message) {
        rabbitTemplate.convertAndSend(workerExchangeName, workerDeletedRoutingKey, message);
    }
}
