package com.ticketshall.tickets.models.nonStoredModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Setter
@Getter
@AllArgsConstructor
public class ReservationItem {
    private UUID ticketTypeId;
    private String ticketTypeName;
    private int quantity;
    private float unitPrice;
}
