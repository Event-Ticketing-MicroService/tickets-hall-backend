package com.ticketshall.attendance.mq.events;

import java.util.UUID;

public record TicketDeletedEvent(UUID id) {
}
