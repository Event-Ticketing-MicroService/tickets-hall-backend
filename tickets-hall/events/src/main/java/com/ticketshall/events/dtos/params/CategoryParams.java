package com.ticketshall.events.dtos.params;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CategoryParams {
    @NotNull(message = "name is required")
    @Size(min = 5, max = 20, message = "category must be 3-20 characters")
    private String name;
}
