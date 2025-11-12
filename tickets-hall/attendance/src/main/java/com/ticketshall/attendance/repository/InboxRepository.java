package com.ticketshall.attendance.repository;

import com.ticketshall.attendance.entity.InboxMessage;
import com.ticketshall.attendance.entity.id.InboxMessageId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxRepository extends JpaRepository<InboxMessage, InboxMessageId> {
}
