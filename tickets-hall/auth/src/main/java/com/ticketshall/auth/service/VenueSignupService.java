package com.ticketshall.auth.service;

import com.ticketshall.auth.DTO.VenueRequestDTO;
import com.ticketshall.auth.DTO.VenueResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface VenueSignupService {
    VenueResponseDTO createVenue(VenueRequestDTO venueRequestDTO, MultipartFile image);
}
