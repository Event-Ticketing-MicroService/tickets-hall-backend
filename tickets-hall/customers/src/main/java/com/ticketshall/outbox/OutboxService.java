package com.ticketshall.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OutboxService {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }


    @Transactional
    public void saveEvent(String eventType, Long aggregateId, Object eventPayload, String routingKey, String exchange) {
        try {
            String payload = objectMapper.writeValueAsString(eventPayload);

            OutboxEvent outboxEvent = new OutboxEvent(eventType, aggregateId, payload, routingKey, exchange);
            outboxRepository.save(outboxEvent);
            log.debug("Saved event to outbox: {} (aggregateId={})", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event payload for {}: {}", eventType, e.getMessage());
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
}
