package com.ticketshall.tickets.exceptions.handlers;

import com.ticketshall.tickets.dto.ErrorResponse;
import com.ticketshall.tickets.exceptions.TicketTypeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
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

}
//LocalDateTime timestamp,
//int status,
//String error,
//String message,
//String path){
