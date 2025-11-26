package com.ticketshall.auth.validators;

import com.ticketshall.auth.DTO.SignupRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SignupRequestValidator implements ConstraintValidator<ValidSignupRequest, SignupRequestDTO> {

    @Override
    public boolean isValid(SignupRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        boolean valid = true;

        context.disableDefaultConstraintViolation();

        switch (dto.userType()) {

            case VENUE -> {
                if (dto.venueDetails() == null) {
                    context.buildConstraintViolationWithTemplate(
                            "Venue details are required when user type is VENUE"
                    ).addPropertyNode("venueDetails").addConstraintViolation();
                    valid = false;
                }
            }

            case CUSTOMER -> {
                if (dto.customerDetails() == null) {
                    context.buildConstraintViolationWithTemplate(
                            "Customer details are required when user type is CUSTOMER"
                    ).addPropertyNode("customerDetails").addConstraintViolation();
                    valid = false;
                }
            }
        }

        return valid;
    }
}

