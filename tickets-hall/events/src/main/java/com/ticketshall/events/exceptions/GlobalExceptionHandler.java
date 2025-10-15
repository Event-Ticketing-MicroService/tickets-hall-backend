package com.ticketshall.events.exceptions;

import com.ticketshall.events.dtos.responses.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomErrorException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomErrorException(CustomErrorException e) {
        log.info("Custom Error: {}", e.getMessage(), e);
        ApiErrorResponse errorResponse = new ApiErrorResponse(e.getKey(), e.getMessage(), e.getHttpStatusCode().value());
        return ResponseEntity.status(e.getHttpStatusCode()).body(errorResponse);
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse("RESOURCE_NOT_FOUND", e.getMessage(), e.getStatusCode().value());
        return ResponseEntity.status(e.getStatusCode()).body(apiErrorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("Method Not Allowed: {}", e.getMessage(), e);
        ApiErrorResponse errorResponse = new ApiErrorResponse("METHOD_NOT_ALLOWED", "HTTP method " + e.getMethod() + " is not supported for this endpoint", HttpStatus.METHOD_NOT_ALLOWED.value());
        return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.info("Validations Error: {}", e.getMessage(), e);
        String firstError = "invalid field '" + e.getFieldErrors().get(0).getField() + "': " + e.getFieldErrors().get(0).getDefaultMessage();
        ApiErrorResponse errorResponse = new ApiErrorResponse("BAD_REQUEST", firstError, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception e) {
        log.error("Internal Server Error: {}", e.getMessage(), e);
        ApiErrorResponse errorResponse = new ApiErrorResponse("INTERNAL_SERVER_ERROR", "Something Went Wrong", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse);
    }
}
