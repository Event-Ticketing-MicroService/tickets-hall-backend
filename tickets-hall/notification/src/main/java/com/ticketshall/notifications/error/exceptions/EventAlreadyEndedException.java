package com.ticketshall.notifications.error.exceptions;

public class EventAlreadyEndedException extends RuntimeException {
    public EventAlreadyEndedException(String message) {
        super(message);
    }
}
