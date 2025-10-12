package com.ticketshall.events.dtos.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateEventParams {
    private int categoryId;
    private String title;
    private String description;
}
