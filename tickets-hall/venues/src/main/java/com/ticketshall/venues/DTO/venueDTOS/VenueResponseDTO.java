package com.ticketshall.venues.DTO.venueDTOS;

import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private String venueCountry;
    private String venueImageUrl;
    private List<String> venueWorkers;
}