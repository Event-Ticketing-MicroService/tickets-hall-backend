package com.ticketshall.venues.schedulers;

import com.ticketshall.venues.constants.GeneralConstants;
import com.ticketshall.venues.helpers.JsonUtil;
import com.ticketshall.venues.model.OutboxMessage;
import com.ticketshall.venues.mq.events.WorkerCreatedMessage;
import com.ticketshall.venues.mq.producers.VenueWorkerProducer;
import com.ticketshall.venues.repository.OutboxRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkerCreatedOutboxScheduler {
    private final JsonUtil jsonUtil;
    private final OutboxRepo outboxRepository;
    private final VenueWorkerProducer workerProducer;

    @Scheduled(fixedRate = 10000)
    public void sendPendingCreatedWorkers() {
        List<OutboxMessage> messages = outboxRepository.findTop10ByProcessedFalseAndType(GeneralConstants.WORKER_CREATED_OUTBOX_TYPE);
        for (OutboxMessage message : messages) {
            workerProducer.sendWorkerCreated(jsonUtil.fromJson(message.getPayload(), WorkerCreatedMessage.class));
            message.setProcessed(true);
            outboxRepository.save(message);
        }
    }
}
