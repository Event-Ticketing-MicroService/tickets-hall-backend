package com.ticketshall.venues.controller;

import com.ticketshall.venues.DTO.venueDTOS.VenueRequestDTO;
import com.ticketshall.venues.DTO.venueDTOS.VenueResponseDTO;
import com.ticketshall.venues.DTO.venueDTOS.VenuePatchDTO;
import com.ticketshall.venues.DTO.venueImgDTOS.VenueImageRequestDTO;
import com.ticketshall.venues.DTO.venueImgDTOS.VenueImageResponseDTO;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerPatchDTO;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerRequestDTO;
import com.ticketshall.venues.DTO.venueWorkerDTOS.WorkerResponseDTO;
import com.ticketshall.venues.service.VenueImgService;
import com.ticketshall.venues.service.VenueService;
import com.ticketshall.venues.service.VenueWorkerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/venues")
public class VenueController {
    private final VenueService venueService;
    private final VenueImgService venueImgService;
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
    @PostMapping
    public ResponseEntity<VenueResponseDTO> createVenue(@RequestBody @Validated VenueRequestDTO venueRequestDTO){
        return ResponseEntity.ok().body(venueService.createVenue(venueRequestDTO));
    }

    @Operation(summary = "Updated a Venue")
    @PatchMapping("/{id}")
    public ResponseEntity<VenueResponseDTO> updateVenue(@PathVariable Long id,@Validated @RequestBody VenuePatchDTO venueRequestDTO){
        return ResponseEntity.ok().body(venueService.updateVenue(venueRequestDTO,id));
    }

    @Operation(summary = "Deleted a Venue")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id){
        if (venueService.getVenueById(id).isEmpty())throw new RuntimeException("Venue not found");
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all venue images")
    @GetMapping("/{id}/images")
    public ResponseEntity<List<VenueImageResponseDTO>> getAllVenueImages(@PathVariable Long id){
        return ResponseEntity.ok().body(venueImgService.getAllVenueImages(id));
    }

    @Operation(summary = "Added images to a Venue")
    @PatchMapping("/{id}/images")
    public ResponseEntity<List<VenueImageResponseDTO>> addPhotos(@PathVariable Long id, @RequestBody @Validated List<VenueImageRequestDTO> imageRequestDTO){
        return ResponseEntity.ok().body(venueImgService.addImagesToVenue(id, imageRequestDTO));
    }

    //URL ID is the venue id, Img ID to be deleted should be included in the json request body
    @Operation(summary = "Deleted images from a Venue")
    @DeleteMapping("/{id}/images")
    public ResponseEntity<Void> deletePhotos(@PathVariable Long id , @RequestBody List<Long> imageIdsToDelete){
        venueImgService.deleteVenueImages(imageIdsToDelete);
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
