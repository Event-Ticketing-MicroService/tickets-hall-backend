package com.ticketshall.auth.exceptions_handlers;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
