package com.ticketshall.tickets.dto;

import java.util.UUID;

public record ReservationRequestItem(
        UUID ticketTypeId,
        int quantity) {
}
