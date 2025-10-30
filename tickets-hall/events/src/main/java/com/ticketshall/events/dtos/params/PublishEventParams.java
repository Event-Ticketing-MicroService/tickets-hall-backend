package com.ticketshall.events.dtos.params;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PublishEventParams {
    @NotNull(message = "isPublished is required")
    Boolean isPublished;
}
