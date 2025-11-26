package com.ticketshall.auth.exceptions_handlers;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
