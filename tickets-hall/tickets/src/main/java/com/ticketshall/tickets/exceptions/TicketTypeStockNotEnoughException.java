package com.ticketshall.tickets.exceptions;

public class TicketTypeStockNotEnoughException extends RuntimeException {
    public TicketTypeStockNotEnoughException(String message) {
        super(message);
    }
}
