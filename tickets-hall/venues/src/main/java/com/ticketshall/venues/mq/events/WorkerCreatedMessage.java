package com.ticketshall.venues.mq.events;

import lombok.Builder;

@Builder
public record WorkerCreatedMessage(
        String email,
        String password
) {
}
