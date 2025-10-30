package com.ticketshall.events.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictErrorException extends CustomErrorException {
    public ConflictErrorException(String message) {
        super("CONFLICT", message, HttpStatus.CONFLICT);
    }
}
