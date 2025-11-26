package com.ticketshall.auth.service.impl;

import com.ticketshall.auth.DTO.VenueRequestDTO;
import com.ticketshall.auth.DTO.VenueResponseDTO;
import com.ticketshall.auth.exceptions_handlers.ExternalServiceException;
import com.ticketshall.auth.feign.VenueClient;
import com.ticketshall.auth.service.VenueSignupService;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class VenueSignupServiceImpl implements VenueSignupService {
    private final VenueClient venueClient;

    @Override
    @Retry(name = "venueServiceRetry", fallbackMethod = "venueFallback")
    public VenueResponseDTO createVenue(VenueRequestDTO venueRequestDTO, MultipartFile image) {
        return venueClient.createVenue(venueRequestDTO, image);
    }

    public VenueResponseDTO venueFallback(VenueRequestDTO dto, MultipartFile image, Throwable ex) {
        throw new ExternalServiceException("Venue Service unavailable. Try again later.");
    }
}
