package com.ticketshall.events.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends CustomErrorException {
    public BadRequestException(String message) {
        super("BAD_REQUEST", message, HttpStatus.BAD_REQUEST);
    }
}
