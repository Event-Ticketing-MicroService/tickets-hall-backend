package com.ticketshall.attendance.events;

import java.util.UUID;

public record TicketUpdatedEvent(UUID id, String code, UUID userId, UUID eventId) {
}
