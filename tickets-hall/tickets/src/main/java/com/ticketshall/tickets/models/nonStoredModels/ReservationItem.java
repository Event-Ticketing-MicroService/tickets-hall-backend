package com.ticketshall.tickets.models.nonStoredModels;

import lombok.AllArgsConstructor;

import java.util.UUID;
@AllArgsConstructor
public class ReservationItem {
    private UUID ticketTypeId;
    private String ticketTypeName;
    private int quantity;
    private float unitPrice;
}
