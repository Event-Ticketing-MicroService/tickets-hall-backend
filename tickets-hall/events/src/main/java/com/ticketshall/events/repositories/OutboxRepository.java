package com.ticketshall.events.repositories;

import com.ticketshall.events.models.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findByProcessedFalseAndType(String type);
}
