package com.ticketshall.auth.feign;

import com.ticketshall.auth.exceptions_handlers.*;
import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        int status = response.status();

        switch (status) {
            case 400:
                return new BadRequestException("Invalid request sent to Venue Service");
            case 401:
                return new UnauthorizedException("Not authorized to access Venue Service");
            case 403:
                return new ForbiddenException("Forbidden action on Venue Service");
            case 404:
                return new ResourceNotFoundException("Resource not found in Venue Service");
            case 500:
            case 503:
                return new ExternalServiceException("Venue Service is temporarily unavailable");
            default:
                return new RuntimeException("Unknown error from Venue Service");
        }

    }
}
