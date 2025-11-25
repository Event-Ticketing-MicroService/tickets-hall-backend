package com.ticketshall.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.outbox.max-retries:5}")
    private Integer maxRetries;

    @Scheduled(fixedDelayString = "${app.outbox.publish-interval:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents(maxRetries);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending events to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                rabbitTemplate.convertAndSend(
                    event.getExchange(),
                    event.getRoutingKey(),
                    event.getPayload()
                );

                event.markAsPublished();
                outboxRepository.save(event);

                log.info("Successfully published event: {} (ID: {})", event.getEventType(), event.getId());

            } catch (Exception e) {
                event.incrementRetryCount();

                if (event.getRetryCount() >= maxRetries) {
                    event.markAsFailed();
                    log.error("Event {} (ID: {}) failed after {} retries: {}",
                        event.getEventType(), event.getId(), maxRetries, e.getMessage());
                } else {
                    log.warn("Failed to publish event {} (ID: {}), retry {}/{}: {}",
                        event.getEventType(), event.getId(), event.getRetryCount(), maxRetries, e.getMessage());
                }

                outboxRepository.save(event);
            }
        }
    }
}
