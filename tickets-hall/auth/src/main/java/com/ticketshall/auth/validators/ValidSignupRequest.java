package com.ticketshall.auth.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SignupRequestValidator.class)
public @interface ValidSignupRequest {
    String message() default "Either venueDetails or customerDetails must be provided, but not both";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
