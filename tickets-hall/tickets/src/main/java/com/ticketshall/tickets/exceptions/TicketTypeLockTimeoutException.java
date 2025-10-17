package com.ticketshall.tickets.exceptions;

public class TicketTypeLockTimeoutException extends RuntimeException {
    public TicketTypeLockTimeoutException(String message) {
        super(message);
    }
}
