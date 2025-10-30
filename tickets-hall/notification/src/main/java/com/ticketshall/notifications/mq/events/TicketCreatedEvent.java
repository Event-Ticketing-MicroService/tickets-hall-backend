package com.ticketshall.notifications.mq.events;

import java.util.UUID;

public record TicketCreatedEvent(UUID id, String code, UUID userId, UUID eventId) {}

