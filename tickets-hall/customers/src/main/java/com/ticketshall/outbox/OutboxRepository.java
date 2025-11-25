package com.ticketshall.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'PENDING' AND o.retryCount < :maxRetries ORDER BY o.createdAt ASC")
    List<OutboxEvent> findPendingEvents(Integer maxRetries);

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}

