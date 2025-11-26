package com.ticketshall.auth.feign;

import com.ticketshall.auth.DTO.VenueRequestDTO;
import com.ticketshall.auth.DTO.VenueResponseDTO;
import com.ticketshall.auth.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "venue-service",
        url = "http://venue-service/api/venues",
        configuration = FeignConfig.class
)
public interface VenueClient {
    @PostMapping(value = "",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    VenueResponseDTO createVenue(
            @RequestPart("data") VenueRequestDTO venueRequestDTO,
            @RequestPart("image") MultipartFile image
    );
}
