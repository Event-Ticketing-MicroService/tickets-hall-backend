package com.ticketshall.attendance.error.exceptions;

public class TicketAlreadyUsedException extends RuntimeException {
    public TicketAlreadyUsedException(String message)
    {
        super(message);
    }
}
