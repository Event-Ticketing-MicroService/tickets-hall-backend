package com.ticketshall.outbox;

public enum OutboxStatus {
    PENDING,    // Event waiting to be published
    PUBLISHED,  // Event successfully published
    FAILED      // Event failed after max retries
}

