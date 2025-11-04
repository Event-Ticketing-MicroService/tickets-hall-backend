package com.ticketshall.venues.DTO.venueDTOS;


import com.ticketshall.venues.DTO.venueImgDTOS.VenueImageRequestDTO;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerRequestDTO;
import com.ticketshall.venues.model.VenueImage;
import com.ticketshall.venues.model.VenueWorker;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueRequestDTO {

    @NotBlank(message = "Venue name is required")
    private String venueName;

    @NotBlank(message = "Venue address is required")
    private String venueAddress;

    @NotBlank(message = "Phone number is required")
    private String venuePhone;

    @NotBlank(message = "Venue email is required")
    @Email(message = "Venue email must be valid")
    private String venueEmail;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @Positive(message = "Capacity must be greater than 0")
    private int venueCapacity;

    @NotBlank(message = "Description is required")
    private String venueDescription;

    @NotBlank(message = "Country is required")
    private String venueCountry;

    @Size(min = 1, message = "At least one image URL is required")
    private List<VenueImageRequestDTO> venueImages;

    @Size(min = 1, message = "At least one worker is required")
    private List<WorkerRequestDTO> venueWorkers;


}
