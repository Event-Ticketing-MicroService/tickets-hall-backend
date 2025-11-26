package com.ticketshall.auth.exceptions_handlers;

public class MissingJwtException extends RuntimeException {
    public MissingJwtException(String message) {
        super(message);
    }
}
