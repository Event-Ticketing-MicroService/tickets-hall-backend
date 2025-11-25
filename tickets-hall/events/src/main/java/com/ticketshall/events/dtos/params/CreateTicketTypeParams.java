package com.ticketshall.events.dtos.params;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CreateTicketTypeParams {
    @NotEmpty(message = "ticket type is required")
    @Size(min = 3, max = 15, message = "name must be 3-15 characters long")
    private String name;

    @NotNull(message = "price is required")
    @Min(value = 1, message = "price must at least cost 1 units")
    private BigDecimal price;

    @NotNull(message = "quanitiy is required")

    @Min(value = 1, message = "quantity must at least be 1")
    private Integer quantity;

    private String planDescription;
}
