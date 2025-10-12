package com.ticketshall.tickets.mq.events;

import java.util.UUID;

public record TicketDeletedEvent(UUID id) {
}
