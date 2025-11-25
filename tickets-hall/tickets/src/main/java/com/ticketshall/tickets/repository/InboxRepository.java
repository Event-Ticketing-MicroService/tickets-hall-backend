package com.ticketshall.tickets.repository;

import com.ticketshall.tickets.models.InboxMessage;
import com.ticketshall.tickets.models.id.InboxMessageId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxRepository extends JpaRepository<InboxMessage, InboxMessageId> {
}
