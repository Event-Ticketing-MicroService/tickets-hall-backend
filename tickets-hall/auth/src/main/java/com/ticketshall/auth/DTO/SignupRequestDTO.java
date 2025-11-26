package com.ticketshall.auth.DTO;

import com.ticketshall.auth.Enums.UserType;
import com.ticketshall.auth.validators.ValidPassword;
import com.ticketshall.auth.validators.ValidSignupRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@ValidSignupRequest
public record SignupRequestDTO(
        @NotBlank(message = "Email is required")
        String email,
        @ValidPassword
        String password,
        @NotBlank(message = "User type is required")
        @Pattern(regexp = "^(CUSTOMER|VENUE)$", message = "User type must be CUSTOMER or VENUE")
        UserType userType,
        @Valid
        VenueRequestDTO venueDetails,
        @Valid
        CreateCustomerDTO customerDetails
) {
}
