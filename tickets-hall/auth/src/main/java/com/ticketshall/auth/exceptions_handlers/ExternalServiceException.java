package com.ticketshall.auth.exceptions_handlers;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) {
        super(message);
    }
}
