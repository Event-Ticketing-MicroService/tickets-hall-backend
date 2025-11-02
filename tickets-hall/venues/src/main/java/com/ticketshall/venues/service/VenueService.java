package com.ticketshall.venues.service;

import com.ticketshall.venues.DTO.DTOMapper;
import com.ticketshall.venues.DTO.venueDTOS.VenueRequestDTO;
import com.ticketshall.venues.DTO.venueDTOS.VenueResponseDTO;
import com.ticketshall.venues.DTO.venueDTOS.VenuePatchDTO;
import com.ticketshall.venues.model.Venue;
import com.ticketshall.venues.repository.VenueImgRepo;
import com.ticketshall.venues.repository.VenueRepo;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VenueService {
    private final VenueRepo venueRepo;
    private final VenueImgRepo venueImgRepo;

    @Transactional(readOnly = true)
    public List<VenueResponseDTO> getAllVenues() {
        return venueRepo.findAll().stream().map(DTOMapper::toVenueResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public Optional<VenueResponseDTO> getVenueById(Long venueId) {
        return venueRepo.findById(venueId).map(DTOMapper::toVenueResponseDTO);
    }

    @Transactional
    public VenueResponseDTO createVenue(VenueRequestDTO venueRequestDTO) {
        Venue venue = DTOMapper.toVenue(venueRequestDTO);
        venueRepo.save(venue);
        return DTOMapper.toVenueResponseDTO(venue);
    }

    @Transactional
    public VenueResponseDTO updateVenue(VenuePatchDTO venuePatchDTO, Long venueId) {
        Venue venue = venueRepo.findById(venueId).orElseThrow(() -> new IllegalArgumentException("VenueId not found"));
        if (venuePatchDTO.getVenueName() != null) venue.setVenueName(venuePatchDTO.getVenueName());
        if (venuePatchDTO.getVenueAddress() != null) venue.setVenueAddress(venuePatchDTO.getVenueAddress());
        if (venuePatchDTO.getVenuePhone() != null) venue.setVenuePhone(venuePatchDTO.getVenuePhone());
        if (venuePatchDTO.getVenueCapacity() != null) venue.setVenueCapacity(venuePatchDTO.getVenueCapacity());
        if (venuePatchDTO.getVenueDescription() != null) venue.setVenueDescription(venuePatchDTO.getVenueDescription());
        if (venuePatchDTO.getVenueCountry() != null) venue.setVenueCountry(venuePatchDTO.getVenueCountry());

        venueRepo.save(venue);

        return DTOMapper.toVenueResponseDTO(venue);
    }

    @Transactional
    public void deleteVenue(Long venueId) {
        venueRepo.deleteById(venueId);
    }



}
