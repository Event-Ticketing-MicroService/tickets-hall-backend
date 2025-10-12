package com.ticketshall.tickets.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String code; // type-code
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Attendee attendee;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Event event;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private TicketType ticketType;
}
