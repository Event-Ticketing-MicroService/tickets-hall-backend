package com.ticketshall.events.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends CustomErrorException {
    public NotFoundException(String message) {
        super("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}
