package com.ticketshall.auth.repository;

import com.ticketshall.auth.model.InboxMessage;
import com.ticketshall.auth.model.id.InboxMessageId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxRepository extends JpaRepository<InboxMessage, InboxMessageId> {
}
