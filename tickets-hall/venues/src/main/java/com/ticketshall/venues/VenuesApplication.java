package com.ticketshall.venues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VenuesApplication {
    public static void main(String[] args) {
        SpringApplication.run(VenuesApplication.class, args);
        System.out.println("Venues Application Started");
    }
}