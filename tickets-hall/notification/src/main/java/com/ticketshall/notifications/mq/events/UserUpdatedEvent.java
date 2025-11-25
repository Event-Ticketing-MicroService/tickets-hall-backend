package com.ticketshall.notifications.mq.events;

import java.util.UUID;

public record UserUpdatedEvent(UUID id, String firstName, String lastName, String email) {

}
