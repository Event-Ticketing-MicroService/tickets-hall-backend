package com.ticketshall.events.exceptions;

import org.springframework.http.HttpStatus;

public class InternalServerError extends CustomErrorException{
    InternalServerError(String message) {
        super("INTERNAL_SERVER_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
