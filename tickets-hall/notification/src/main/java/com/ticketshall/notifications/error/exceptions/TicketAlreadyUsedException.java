package com.ticketshall.notifications.error.exceptions;

public class TicketAlreadyUsedException extends RuntimeException {
    public TicketAlreadyUsedException(String message)
    {
        super(message);
    }
}
