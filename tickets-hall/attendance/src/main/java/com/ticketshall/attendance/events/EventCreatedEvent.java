package com.ticketshall.attendance.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventCreatedEvent(UUID id, String title, String description, String location, LocalDateTime startsAtUTc, LocalDateTime endsAtUtc) {}
