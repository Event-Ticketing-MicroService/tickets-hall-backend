package com.ticketshall.events.exceptions;

import org.springframework.http.HttpStatus;

public class UnAuthorizedException extends CustomErrorException{
    public UnAuthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }
}
