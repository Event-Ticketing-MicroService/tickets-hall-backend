package com.ticketshall.events.dtos.params;

import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.UUID;
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class UpsertEventParams {
    @NotNull(message = "category is required")
    private UUID categoryId;

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "description is required")
    private String description;


    @NotBlank(message = "location is required")
    @Size(min = 10, message = "location details must be at least 10 characters long")
    private String location;


    @NotNull(message = "latitude is required")
    private Double latitude;


    @NotNull(message = "longitude is required")
    private Double longitude;


    @NotNull(message = "startsAt date is required")
    @Future(message = "Event starting date must be in the future")
    private LocalDateTime startsAt;


    @NotNull(message = "endsAt date is required")
    @Future(message = "Event ending date must be in the future")
    private LocalDateTime endsAt;


    @NotNull(message = "totalAvailableTickets quantity is required")
    @Min(value = 0, message = "total available tickets cannot be negative")
    private Integer totalAvailableTickets;
}
