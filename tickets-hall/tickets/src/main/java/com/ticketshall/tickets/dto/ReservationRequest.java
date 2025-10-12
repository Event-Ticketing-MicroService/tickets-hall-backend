package com.ticketshall.tickets.dto;

import java.util.List;
import java.util.UUID;

public record ReservationRequest(
        UUID eventId,
        UUID attendeeId,
        List<ReservationRequestItem> items) {
}
