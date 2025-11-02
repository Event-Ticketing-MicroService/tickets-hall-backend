package com.ticketshall.venues.DTO.venueWorkerDTOS;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerResponseDTO {
    private Long workerId;
    private String fullName;
    private String username;
    private String email;
    private Long venueId;
    private String venueName;
}