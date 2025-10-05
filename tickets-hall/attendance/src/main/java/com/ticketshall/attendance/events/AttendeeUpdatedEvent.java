package com.ticketshall.attendance.events;

import java.util.UUID;

public record AttendeeUpdatedEvent(UUID id, String firstName, String lastName, String email) {
}
