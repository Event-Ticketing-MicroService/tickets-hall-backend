package com.ticketshall.venues.service;

import com.ticketshall.venues.DTO.DTOMapper;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerPatchDTO;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerRequestDTO;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerResponseDTO;
import com.ticketshall.venues.model.Venue;
import com.ticketshall.venues.model.VenueWorker;
import com.ticketshall.venues.repository.VenueRepo;
import com.ticketshall.venues.repository.VenueWorkerRepo;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueWorkerService {
    private final VenueWorkerRepo venueWorkerRepo;
    private final VenueRepo venueRepo;

    @Transactional(readOnly = true)
    public List<WorkerResponseDTO> getAllVenueWorkers( Long venueId ) {
        Venue venue = venueRepo.findById(venueId).orElseThrow(() -> new IllegalArgumentException("Venue not found"));;
        return venue.getWorkers().stream().map(DTOMapper::toWorkerResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public WorkerResponseDTO getVenueWorker(Long workerId, Long venueId) {
        Venue venue = venueRepo.findById(venueId).orElseThrow(() -> new IllegalArgumentException("Venue not found"));

        return venue.getWorkers().stream()
                .filter(worker -> worker.getWorkerId().equals(workerId))
                .findFirst()
                .map(DTOMapper::toWorkerResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found for this venue"));
    }

    @Transactional
    public List <WorkerResponseDTO> addVenueWorker(List <WorkerRequestDTO> workerRequestDTO, Long venueId) {
        Venue venue = venueRepo.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found"));

        List<VenueWorker> existingWorkers = venue.getWorkers() != null
                ? venue.getWorkers()
                : new ArrayList<>();

        List<VenueWorker> newWorkers = workerRequestDTO.stream().map(
                dto-> VenueWorker.builder()
                        .workerEmail(dto.getEmail())
                        .workerName(dto.getFullName())
                        .venue(venue)
                        .build()
        ).toList();

        existingWorkers.addAll(newWorkers);
        venue.setWorkers(existingWorkers);
        venueRepo.save(venue);

        return existingWorkers.stream().map(DTOMapper::toWorkerResponseDTO).toList();
    }

    @Transactional
    public void deleteVenueWorker(List<Long> workerIds) {
        if (venueWorkerRepo.findAllById(workerIds).isEmpty())
            throw new IllegalArgumentException("Venue worker(s) not found");
        venueWorkerRepo.deleteAllById(workerIds);
    }

    @Transactional
    public WorkerResponseDTO patchVenueWorker(WorkerPatchDTO workerPatchDTO) {
        VenueWorker worker = venueWorkerRepo.findById(workerPatchDTO.getWorkerId()).orElseThrow(() -> new IllegalArgumentException("Worker not found"));
        if(workerPatchDTO.getEmail() != null)  worker.setWorkerEmail(workerPatchDTO.getEmail());
        if(workerPatchDTO.getFullName() != null)  worker.setWorkerName(workerPatchDTO.getFullName());

        venueWorkerRepo.save(worker);
        return DTOMapper.toWorkerResponseDTO(worker);
    }

}
