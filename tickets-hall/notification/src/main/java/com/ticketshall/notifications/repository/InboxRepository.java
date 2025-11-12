package com.ticketshall.notifications.repository;

import com.ticketshall.notifications.entity.InboxMessage;
import com.ticketshall.notifications.entity.id.InboxMessageId;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InboxRepository extends JpaRepository<InboxMessage, InboxMessageId> {
}
