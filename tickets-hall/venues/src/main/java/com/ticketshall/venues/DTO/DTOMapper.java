package com.ticketshall.venues.DTO;

import com.ticketshall.venues.DTO.venueDTOS.VenueRequestDTO;
import com.ticketshall.venues.DTO.venueDTOS.VenueResponseDTO;
import com.ticketshall.venues.DTO.venueImgDTOS.VenueImageRequestDTO;
import com.ticketshall.venues.DTO.venueImgDTOS.VenueImageResponseDTO;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerRequestDTO;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerResponseDTO;
import com.ticketshall.venues.model.Venue;
import com.ticketshall.venues.model.VenueImage;
import com.ticketshall.venues.model.VenueWorker;
import com.ticketshall.venues.repository.VenueRepo;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DTOMapper {

    private final VenueRepo venueRepo;

    public static VenueResponseDTO toVenueResponseDTO(Venue venue) {

        return VenueResponseDTO.builder()
                .venueID(venue.getVenueID())
                .venueAddress(venue.getVenueAddress())
                .venueName(venue.getVenueName())
                .venueCapacity(venue.getVenueCapacity())
                .venueCountry(venue.getVenueCountry())
                .venuePhone(venue.getVenuePhone())
                .venueEmail(venue.getVenueEmail())
                .venueImages(
                        venue.getVenueImages().stream()
                                .map(img -> new VenueImageResponseDTO(img.getVenueImageID(), img.getImageURL()))
                                .toList()
                )
                .venueDescription(venue.getVenueDescription())
                .venueWorkers(
                        venue.getWorkers() != null
                                ? venue.getWorkers().stream().map(VenueWorker::getUsername).toList()
                                : null
                )
                .build();
    }

    public static Venue toVenue(VenueRequestDTO venueRequestDTO) {
         Venue venue= Venue.builder()
                 .venueName(venueRequestDTO.getVenueName())
                 .venueDescription(venueRequestDTO.getVenueDescription())
                 .venueAddress(venueRequestDTO.getVenueAddress())
                 .venueCapacity(venueRequestDTO.getVenueCapacity())
                 .venueCountry(venueRequestDTO.getVenueCountry())
                 .venuePhone(venueRequestDTO.getVenuePhone())
                 .venueEmail(venueRequestDTO.getVenueEmail())
                 .build();
        if (venueRequestDTO.getVenueImages() != null) {
            List<VenueImage> images = venueRequestDTO.getVenueImages().stream()
                    .map(imgDTO -> VenueImage.builder()
                            .imageURL(imgDTO.getImageURL())
                            .venue(venue)
                            .build())
                    .toList();
            venue.setVenueImages(images);
        }

        if (venueRequestDTO.getVenueWorkers() != null) {
            List<VenueWorker> venueWorkers = venueRequestDTO.getVenueWorkers().stream()
                    .map(workerDto -> VenueWorker.builder()
                            .workerName(workerDto.getFullName())
                            .username(workerDto.getUsername())
                            .workerEmail(workerDto.getEmail())
                            .venue(venue)
                            .build())
                    .toList();

            venue.setWorkers(venueWorkers);
        }

        return venue;
    }

    public static VenueImageResponseDTO toVenueImageResponseDTO(VenueImage venueImage) {
        return VenueImageResponseDTO.builder()
                .venueImageID(venueImage.getVenueImageID())
                .imageURL(venueImage.getImageURL())
                .build();
    }

    public static VenueImage toVenueImage(VenueImageRequestDTO venueImageRequestDTO) {
        return VenueImage.builder()
                .venueImageID(venueImageRequestDTO.getVenueID())
                .imageURL(venueImageRequestDTO.getImageURL())
                .build();
    }

    public VenueWorker toVenueWorker(WorkerRequestDTO workerRequestDTO) {
        VenueWorker worker = VenueWorker.builder()
                .workerName(workerRequestDTO.getFullName())
                .username(workerRequestDTO.getUsername())
                .workerEmail(workerRequestDTO.getEmail())
                .build();

        worker.setVenue(venueRepo.findById(workerRequestDTO.getVenueId()).orElseThrow());

        return worker;
    }

    public static WorkerResponseDTO toWorkerResponseDTO(VenueWorker venueWorker) {
        return WorkerResponseDTO.builder()
                .workerId(venueWorker.getWorkerId())
                .username(venueWorker.getUsername())
                .venueName(venueWorker.getVenue().getVenueName())
                .fullName(venueWorker.getWorkerName())
                .email(venueWorker.getWorkerEmail())
                .venueId(venueWorker.getVenue().getVenueID())
                .build();
    }

}

