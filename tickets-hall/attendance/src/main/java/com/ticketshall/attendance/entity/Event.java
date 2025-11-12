package com.ticketshall.attendance.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

//TODO: Add constraints
@Setter
@Getter
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
