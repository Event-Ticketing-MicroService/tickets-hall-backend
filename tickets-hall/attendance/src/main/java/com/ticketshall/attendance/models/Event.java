package com.ticketshall.attendance.models;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

//TODO: Add constraints
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class Event {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String location;
    
    @Column(nullable = false)
    private LocalDateTime startsAtUtc;

    @Column(nullable = false)
    private LocalDateTime endsAtUtc;
}
