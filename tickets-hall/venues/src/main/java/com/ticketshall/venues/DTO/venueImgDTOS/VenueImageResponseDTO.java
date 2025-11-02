package com.ticketshall.venues.DTO.venueImgDTOS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueImageResponseDTO {
    private Long venueImageID;
    private String imageURL;
}