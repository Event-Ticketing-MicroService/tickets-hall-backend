package com.ticketshall.auth.exceptions_handlers;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
