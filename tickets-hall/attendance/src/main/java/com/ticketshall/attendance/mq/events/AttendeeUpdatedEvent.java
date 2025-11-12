package com.ticketshall.attendance.mq.events;

import java.util.UUID;

public record AttendeeUpdatedEvent(UUID id, String firstName, String lastName, String email) {
}
