package com.ticketshall.venues.DTO.venueWorkerDTOS;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkerPatchDTO {

    @NotNull
    private Long workerId;

    @Size(min = 2, message = "Full name must have at least 2 characters")
    private String fullName;

    @Size(min = 3, message = "Username must have at least 3 characters")
    private String username;

    @Email(message = "Email must be valid")
    private String email;

    private Long venueId;
}
