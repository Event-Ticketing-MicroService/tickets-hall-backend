package com.ticketshall.payments.repository;

import com.ticketshall.payments.entity.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findByProcessedFalseAndType(String type);
}
