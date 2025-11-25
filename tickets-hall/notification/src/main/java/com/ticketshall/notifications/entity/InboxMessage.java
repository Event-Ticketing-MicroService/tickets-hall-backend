package com.ticketshall.notifications.entity;

import com.ticketshall.notifications.entity.id.InboxMessageId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InboxMessage {
    @EmbeddedId
    private InboxMessageId inboxMessageId;

    @Column(nullable = false)
    private LocalDateTime receivedAt;
}