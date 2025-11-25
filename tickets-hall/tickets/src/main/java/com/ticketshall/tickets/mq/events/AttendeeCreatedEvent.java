package com.ticketshall.tickets.mq.events;

import java.util.UUID;

public record AttendeeCreatedEvent(UUID id, String firstName, String lastName, String email) { }
