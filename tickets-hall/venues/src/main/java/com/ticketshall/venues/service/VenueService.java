package com.ticketshall.venues.service;

import com.ticketshall.venues.DTO.DTOMapper;
import com.ticketshall.venues.DTO.venueDTOS.VenueRequestDTO;
import com.ticketshall.venues.DTO.venueDTOS.VenueResponseDTO;
import com.ticketshall.venues.DTO.venueDTOS.VenuePatchDTO;
import com.ticketshall.venues.model.Venue;
import com.ticketshall.venues.model.VenueImage;
import com.ticketshall.venues.repository.VenueRepo;
import com.ticketshall.venues.service.impl.CloudinaryService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VenueService {
    private final VenueRepo venueRepo;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public List<VenueResponseDTO> getAllVenues() {
        return venueRepo.findAll().stream().map(DTOMapper::toVenueResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public Optional<VenueResponseDTO> getVenueById(Long venueId) {
        return venueRepo.findById(venueId).map(DTOMapper::toVenueResponseDTO);
    }

    @Transactional
    public VenueResponseDTO createVenue(VenueRequestDTO venueRequestDTO, MultipartFile image) {
        Map<String, String> uploadResult = null;
        try {
            Venue venue = DTOMapper.toVenue(venueRequestDTO);
            uploadResult = cloudinaryService.uploadImage(image);

            VenueImage venueImage = VenueImage.builder()
                    .imageURL(uploadResult.get("url"))
                    .publicId(uploadResult.get("public_id"))
                    .venue(venue)
                    .build();

            venue.setVenueImage(venueImage);

            venueRepo.save(venue);

            return DTOMapper.toVenueResponseDTO(venue);
        } catch (Exception e) {
            if (uploadResult != null && uploadResult.get("public_id") != null) {
                cloudinaryService.deleteImage(uploadResult.get("public_id"));
            }
            throw e;
        }
    }

    @Transactional
    public VenueResponseDTO updateVenue(VenuePatchDTO venuePatchDTO, Long venueId, MultipartFile image) {
        Venue venue = venueRepo.findById(venueId).orElseThrow(() -> new IllegalArgumentException("VenueId not found"));
        if (venuePatchDTO.getVenueName() != null) venue.setVenueName(venuePatchDTO.getVenueName());
        if (venuePatchDTO.getVenueAddress() != null) venue.setVenueAddress(venuePatchDTO.getVenueAddress());
        if (venuePatchDTO.getVenuePhone() != null) venue.setVenuePhone(venuePatchDTO.getVenuePhone());
        if (venuePatchDTO.getVenueCapacity() != null) venue.setVenueCapacity(venuePatchDTO.getVenueCapacity());
        if (venuePatchDTO.getVenueDescription() != null) venue.setVenueDescription(venuePatchDTO.getVenueDescription());
        if (venuePatchDTO.getVenueCountry() != null) venue.setVenueCountry(venuePatchDTO.getVenueCountry());
        if (image != null && !image.isEmpty()) {
            VenueImage oldImage = venue.getVenueImage();
            Map<String, String> uploadResult = cloudinaryService.uploadImage(image);
            if (oldImage != null) {
                cloudinaryService.deleteImage(oldImage.getPublicId());
                oldImage.setImageURL(uploadResult.get("url"));
                oldImage.setPublicId(uploadResult.get("public_id"));
                venue.setVenueImage(oldImage);
            } else {
                VenueImage newImage = VenueImage.builder()
                        .imageURL(uploadResult.get("url"))
                        .publicId(uploadResult.get("public_id"))
                        .build();
                venue.setVenueImage(newImage);
            }
        }
        venueRepo.save(venue);

        return DTOMapper.toVenueResponseDTO(venue);
    }

    @Transactional
    public void deleteVenue(Long venueId) {
        venueRepo.deleteById(venueId);
    }



}
