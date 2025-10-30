package com.ticketshall.tickets.exceptions.handlers;

import com.ticketshall.tickets.dto.response.ErrorResponse;
import com.ticketshall.tickets.exceptions.TicketTypeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
// TODO: Create the rest of the exceptions
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TicketTypeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTicketTypeNotFoundException(
            TicketTypeNotFoundException ex,
            WebRequest request)
    {
        ErrorResponse response = new ErrorResponse(
            LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "Ticket Type not found",
                request.getDescription(false).replace("uri=","")
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

}
//LocalDateTime timestamp,
//int status,
//String error,
//String message,
//String path){
