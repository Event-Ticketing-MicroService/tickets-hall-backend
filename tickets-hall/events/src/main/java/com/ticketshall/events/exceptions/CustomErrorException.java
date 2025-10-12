package com.ticketshall.events.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
public class CustomErrorException extends RuntimeException {
    // key: used for handling custom logic depending on the key
    private String key;

    private HttpStatusCode httpStatusCode;

    public CustomErrorException(String key, String message, HttpStatusCode httpStatusCode) {
        super(message);
        this.key = key;
        this.httpStatusCode = httpStatusCode;
    }
}
