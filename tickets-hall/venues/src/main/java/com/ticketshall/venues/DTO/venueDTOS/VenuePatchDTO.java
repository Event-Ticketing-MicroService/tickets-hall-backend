package com.ticketshall.venues.DTO.venueDTOS;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VenuePatchDTO {

    @Size(min = 2, message = "Venue name must have at least 2 characters")
    private String venueName;

    @Size(min = 3, message = "Address must have at least 3 characters")
    private String venueAddress;

    @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
    @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
    private double longitude;

    @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
    @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
    private double latitude;

    // Optional but validated if present
    @Pattern(regexp = "^[0-9]{7,15}$", message = "Phone must be numeric and valid length")
    private String venuePhone;

    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String venuePassword;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer venueCapacity;

    @Size(min = 5, message = "Description must be at least 5 characters long")
    private String venueDescription;

    @Size(min = 2, message = "Country must have at least 2 characters")
    private String venueCountry;

    // Optional lists but validated if present
    @Valid
    private List<@NotBlank(message = "Image URL cannot be empty") String> imageURLs;

    @Valid
    private List<@NotBlank(message = "Worker name cannot be empty") String> workers;
}
