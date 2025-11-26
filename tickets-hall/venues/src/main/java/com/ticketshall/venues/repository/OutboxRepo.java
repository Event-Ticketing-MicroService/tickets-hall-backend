package com.ticketshall.venues.repository;

import com.ticketshall.venues.model.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface OutboxRepo extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findTop10ByProcessedFalseAndType(String type);
}
