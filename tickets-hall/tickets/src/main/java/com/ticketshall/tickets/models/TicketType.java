package com.ticketshall.tickets.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.ValueGenerationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// create ticket types and when purchased, create a ticket object
public final class TicketType {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private String description;
    private Float price;
    private Integer totalStock;
    private Integer availableStock;
    private LocalDateTime reservationsStartsAtUtc;
    private LocalDateTime reservationsEndsAtUtc;
    @CreationTimestamp
    private LocalDateTime createdAtUtc;
    @UpdateTimestamp
    private LocalDateTime updatedAtUtc;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Event event;
    @Column(name = "event_id", insertable = false, updatable = false)
    private UUID eventId;
}