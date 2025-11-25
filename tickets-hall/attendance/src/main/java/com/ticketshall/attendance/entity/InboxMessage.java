package com.ticketshall.attendance.entity;

import com.ticketshall.attendance.entity.id.InboxMessageId;
import jakarta.persistence.*;
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
