package com.ticketshall.venues.service;

import com.ticketshall.venues.DTO.DTOMapper;
import com.ticketshall.venues.DTO.venueImgDTOS.VenueImageRequestDTO;
import com.ticketshall.venues.DTO.venueImgDTOS.VenueImageResponseDTO;
import com.ticketshall.venues.model.Venue;
import com.ticketshall.venues.model.VenueImage;
import com.ticketshall.venues.repository.VenueImgRepo;
import com.ticketshall.venues.repository.VenueRepo;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueImgService {

    private final VenueRepo venueRepo;
    private final VenueImgRepo venueImgRepo;

    @Transactional(readOnly = true)
    public List<VenueImageResponseDTO> getAllVenueImages(Long venueId) {
        Venue venue = venueRepo.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found"));
        return venue.getVenueImages().stream()
                .map(DTOMapper::toVenueImageResponseDTO)
                .toList();
    }

    @Transactional
    public List<VenueImageResponseDTO> addImagesToVenue(Long venueId, List<VenueImageRequestDTO> imageDTOs) {
        Venue venue = venueRepo.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found"));

        List<VenueImage> existingImages = venue.getVenueImages() != null
                ? venue.getVenueImages()
                : new ArrayList<>();

        List<VenueImage> newImages = imageDTOs.stream()
                .map(dto -> VenueImage.builder()
                        .imageURL(dto.getImageURL())
                        .venue(venue)
                        .build())
                .toList();

        existingImages.addAll(newImages);
        venue.setVenueImages(existingImages);
        venueRepo.save(venue);

        return existingImages.stream()
                .map(DTOMapper::toVenueImageResponseDTO)
                .toList();
    }

    @Transactional
    public void deleteVenueImages(List<Long> imageIds) {
        if (venueImgRepo.findAllById(imageIds).isEmpty())
            throw new IllegalArgumentException("Venue image(s) not found");
        venueImgRepo.deleteAllById(imageIds);
    }
}
