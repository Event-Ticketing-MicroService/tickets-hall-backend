package com.ticketshall.auth.exceptions_handlers;

public class MissingImageException extends RuntimeException {
    public MissingImageException(String message) {
        super(message);
    }
}
