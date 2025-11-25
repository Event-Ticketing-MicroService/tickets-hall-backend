package com.ticketshall.events.scheduler;

import com.ticketshall.events.constants.GeneralConstants;
import com.ticketshall.events.dtos.messages.EventUpsertedMessage;
import com.ticketshall.events.helpers.JsonUtil;
import com.ticketshall.events.models.OutboxMessage;
import com.ticketshall.events.rabbitmq.producers.EventProducer;
import com.ticketshall.events.repositories.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventUpdatedOutboxScheduler {
    private final JsonUtil jsonUtil;
    private final OutboxRepository outboxRepository;
    private final EventProducer eventProducer;

    @Scheduled(fixedRate = 10000)
    public void sendPendingUpdatedEvents() {
        List<OutboxMessage> messages = outboxRepository.findTop10ByProcessedFalseAndType(GeneralConstants.EVENT_UPDATED_OUTBOX_TYPE);
        for (OutboxMessage message : messages) {
            eventProducer.sendEventUpdated(jsonUtil.fromJson(message.getPayload(), EventUpsertedMessage.class));
            message.setProcessed(true);
            outboxRepository.save(message);
        }
    }
}
