package com.ticketshall.auth.exceptions_handlers;

public class InvalidJwtException extends RuntimeException {
    public InvalidJwtException(String message) {
        super(message);
    }
}
