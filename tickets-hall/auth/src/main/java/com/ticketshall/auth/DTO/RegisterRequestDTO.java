package com.ticketshall.auth.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter and one number"
    )
    private String password;

    @NotBlank(message = "User type is required")
    @Pattern(
            regexp = "^(CUSTOMER|VENUE|WORKER)$",
            message = "User type must be CUSTOMER, VENUE, or WORKER"
    )
    @NotBlank(message = "User type must be specified")
    private String userType;

    private Map<String, Object> details;
}