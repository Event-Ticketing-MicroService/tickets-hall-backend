package com.ticketshall.venues.DTO;

import com.ticketshall.venues.DTO.venueDTOS.VenueRequestDTO;
import com.ticketshall.venues.DTO.venueDTOS.VenueResponseDTO;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerRequestDTO;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerResponseDTO;
import com.ticketshall.venues.model.Venue;
import com.ticketshall.venues.model.VenueWorker;
import com.ticketshall.venues.repository.VenueRepo;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
                .venueImageUrl(venue.getVenueImage().getImageURL())
                .venueDescription(venue.getVenueDescription())
                .venueWorkers(
                        venue.getWorkers() != null
                                ? venue.getWorkers().stream().map(VenueWorker::getWorkerName).toList()
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

        if (venueRequestDTO.getVenueWorkers() != null) {
            List<VenueWorker> venueWorkers = venueRequestDTO.getVenueWorkers().stream()
                    .map(workerDto -> VenueWorker.builder()
                            .workerName(workerDto.getFullName())
                            .workerEmail(workerDto.getEmail())
                            .venue(venue)
                            .build())
                    .toList();

            venue.setWorkers(venueWorkers);
        }

        return venue;
    }

    public VenueWorker toVenueWorker(WorkerRequestDTO workerRequestDTO) {
        return VenueWorker.builder()
                .workerName(workerRequestDTO.getFullName())
                .workerEmail(workerRequestDTO.getEmail())
                .build();
    }

    public static WorkerResponseDTO toWorkerResponseDTO(VenueWorker venueWorker) {
        return WorkerResponseDTO.builder()
                .workerId(venueWorker.getWorkerId())
                .venueName(venueWorker.getVenue().getVenueName())
                .fullName(venueWorker.getWorkerName())
                .email(venueWorker.getWorkerEmail())
                .venueId(venueWorker.getVenue().getVenueID())
                .build();
    }

}

