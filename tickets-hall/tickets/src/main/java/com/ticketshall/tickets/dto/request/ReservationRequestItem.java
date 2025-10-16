package com.ticketshall.tickets.dto.request;

import java.util.UUID;

public record ReservationRequestItem(
        UUID ticketTypeId,
        int quantity) {
}
