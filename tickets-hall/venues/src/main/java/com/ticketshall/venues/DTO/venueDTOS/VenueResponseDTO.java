package com.ticketshall.venues.DTO.venueDTOS;

import com.ticketshall.venues.DTO.venueImgDTOS.VenueImageResponseDTO;
import com.ticketshall.venues.model.VenueImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueResponseDTO {

    private Long venueID;
    private String venueName;
    private String venueAddress;
    private String venuePhone;
    private String venueEmail;
    private int venueCapacity;
    private String venueDescription;
    private double longitude;
    private double latitude;
    private String venueCountry;
    private List<VenueImageResponseDTO> venueImages;
    private List<String> venueWorkers;
}

