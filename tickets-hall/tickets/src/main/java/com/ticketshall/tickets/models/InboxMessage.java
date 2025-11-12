package com.ticketshall.tickets.models;

import com.ticketshall.tickets.models.id.InboxMessageId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

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
