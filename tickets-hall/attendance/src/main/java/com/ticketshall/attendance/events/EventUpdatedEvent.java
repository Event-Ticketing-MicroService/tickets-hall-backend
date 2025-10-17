package com.ticketshall.attendance.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventUpdatedEvent(UUID id, String title, String description, String location, LocalDateTime startsAtUtc, LocalDateTime endsAtUtc) {
}
