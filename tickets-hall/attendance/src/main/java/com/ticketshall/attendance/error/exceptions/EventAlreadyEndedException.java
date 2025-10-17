package com.ticketshall.attendance.error.exceptions;

public class EventAlreadyEndedException extends RuntimeException {
    public EventAlreadyEndedException(String message) {
        super(message);
    }
}
