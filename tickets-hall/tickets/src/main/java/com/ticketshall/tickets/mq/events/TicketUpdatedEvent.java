package com.ticketshall.tickets.mq.events;

import java.util.UUID;

public record TicketUpdatedEvent(UUID id, String code, UUID userId, UUID eventId) {
}
