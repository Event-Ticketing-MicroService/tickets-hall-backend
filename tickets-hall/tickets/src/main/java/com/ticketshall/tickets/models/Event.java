package com.ticketshall.tickets.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class Event {
    @Id
    UUID id;
    String name;
    LocalDateTime ReservationStartsAtUtc;
    LocalDateTime ReservationEndsAtUtc;
    LocalDateTime startDate;
    LocalDateTime endDate;
    String venueName;
    String organizerId;
}
