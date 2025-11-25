package com.ticketshall.tickets.models.nonStoredModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
public class Reservation {
    private UUID id;
    private UUID attendeeId;
    private UUID eventId;
    private String paymentIntentId;
    private List<ReservationItem> items;
    private float totalPrice;
    private LocalDateTime expiresAtUtc;
}
