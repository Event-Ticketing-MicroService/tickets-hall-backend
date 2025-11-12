package com.ticketshall.events.scheduler;

import com.ticketshall.events.constants.GeneralConstants;
import com.ticketshall.events.dtos.messages.EventUpsertedMessage;
import com.ticketshall.events.helpers.JsonUtil;
import com.ticketshall.events.models.OutboxMessage;
import com.ticketshall.events.rabbitmq.producers.EventProducer;
import com.ticketshall.events.repositories.EventRepository;
import com.ticketshall.events.repositories.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventCreatedOutboxScheduler {
    private final JsonUtil jsonUtil;
    private final OutboxRepository outboxRepository;
    private final EventProducer eventProducer;

    @Scheduled(fixedRate = 2000)
    public void sendPendingCreatedEvents() {
        List<OutboxMessage> messages = outboxRepository.findByProcessedFalseAndType(GeneralConstants.EVENT_CREATED_OUTBOX_TYPE);
        for (OutboxMessage message : messages) {
            eventProducer.sendEventCreated(jsonUtil.fromJson(message.getPayload(), EventUpsertedMessage.class));
            message.setProcessed(true);
            outboxRepository.save(message);
        }
    }
}
