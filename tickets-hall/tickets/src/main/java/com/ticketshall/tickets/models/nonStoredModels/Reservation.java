package com.ticketshall.tickets.models.nonStoredModels;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@AllArgsConstructor
public class Reservation {
    private UUID id;
    private UUID attendeeId;
    private UUID eventId;
    private List<ReservationItem> items;
    private float totalPrice;
    private LocalDateTime expiresAtUtc;
}
