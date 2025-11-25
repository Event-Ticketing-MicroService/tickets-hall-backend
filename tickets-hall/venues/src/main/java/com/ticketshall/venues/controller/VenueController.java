package com.ticketshall.venues.controller;

import com.ticketshall.venues.DTO.venueDTOS.VenueRequestDTO;
import com.ticketshall.venues.DTO.venueDTOS.VenueResponseDTO;
import com.ticketshall.venues.DTO.venueDTOS.VenuePatchDTO;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerPatchDTO;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerRequestDTO;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerResponseDTO;
import com.ticketshall.venues.service.VenueService;
import com.ticketshall.venues.service.VenueWorkerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/venues")
public class VenueController {
    private final VenueService venueService;
    private final VenueWorkerService venueWorkerService;

    @Operation(summary = "Get all venues")
    @GetMapping
    public ResponseEntity<List<VenueResponseDTO>> getAllVenues(){
        return ResponseEntity.ok().body(venueService.getAllVenues());
    }

    @Operation(summary = "Get Venue by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Optional<VenueResponseDTO>> getVenueById(@PathVariable Long id){
        return ResponseEntity.ok().body(venueService.getVenueById(id));
    }

    @Operation(summary = "Create a Venue")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VenueResponseDTO> createVenue(
            @RequestPart("data") @Validated VenueRequestDTO venueRequestDTO,
            @RequestPart("image") MultipartFile image
    ){
        return ResponseEntity.ok(venueService.createVenue(venueRequestDTO, image));
    }

    @Operation(summary = "Updated a Venue")
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VenueResponseDTO> updateVenue(
            @PathVariable Long id,
            @Validated @RequestPart("data") VenuePatchDTO venueRequestDTO,
            @RequestPart("image") MultipartFile image){
        return ResponseEntity.ok().body(venueService.updateVenue(venueRequestDTO, id, image));
    }

    @Operation(summary = "Deleted a Venue")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id){
        if (venueService.getVenueById(id).isEmpty())throw new RuntimeException("Venue not found");
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all venue Workers")
    @GetMapping("/{id}/workers")
    public ResponseEntity<List<WorkerResponseDTO>> getAllVenueWorkers(@PathVariable Long id){
        return ResponseEntity.ok().body(venueWorkerService.getAllVenueWorkers(id));
    }

    @Operation(summary = "Added workers to Venue")
    @PostMapping("/{id}/workers")
    public ResponseEntity<List<WorkerResponseDTO>> addWorkers(@PathVariable Long id, @RequestBody @Validated List<WorkerRequestDTO> workerRequestDTO){
        return ResponseEntity.ok().body(venueWorkerService.addVenueWorker(workerRequestDTO,id));
    }

    @Operation(summary = "Delete Workers from a Venue")
    @DeleteMapping("/{venueId}/workers")
    public ResponseEntity<Void> deleteWorkers(
            @RequestBody List<Long> workerIdsToDelete) {
        venueWorkerService.deleteVenueWorker(workerIdsToDelete);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update worker fields")
    @PatchMapping("/{id}/workers")
    public ResponseEntity<WorkerResponseDTO> updateWorker(@RequestBody @Validated WorkerPatchDTO workerPatchDTO, @PathVariable Long id){
        venueWorkerService.patchVenueWorker(workerPatchDTO);
        return ResponseEntity.ok().body(venueWorkerService.patchVenueWorker(workerPatchDTO));
    }
}
