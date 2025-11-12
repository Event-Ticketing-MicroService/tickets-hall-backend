package com.ticketshall.attendance.mq.events;

import java.util.UUID;

public record TicketCreatedEvent(UUID id, String code, UUID userId, UUID eventId) {}
