package com.ticketshall.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class EventsApplication {
    public static void main(String[] args) {
        log.info("Events Service Started...");
        SpringApplication.run(EventsApplication.class, args);
    }
}