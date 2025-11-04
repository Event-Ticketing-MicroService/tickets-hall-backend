package com.ticketshall.venues.DTO.venueImgDTOS;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueImageRequestDTO {
    @NotBlank(message = "Image URL cannot be empty")
    private String imageURL;
    @NotBlank(message =  "Venue ID must be provided")
    private Long venueID;
}