package com.ticketshall.events.dtos.messages;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventUpsertedMessage(UUID id, String title, String description, String location, LocalDateTime startsAtUtc, LocalDateTime endsAtUtc) {}
