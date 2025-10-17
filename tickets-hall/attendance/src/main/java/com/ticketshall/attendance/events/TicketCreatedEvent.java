package com.ticketshall.attendance.events;

import java.util.UUID;

public record TicketCreatedEvent(UUID id, String code, UUID userId, UUID eventId) {}
