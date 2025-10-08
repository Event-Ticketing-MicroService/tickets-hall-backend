package com.ticketshall.venues.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.UUID;

@Entity
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID venueID;

    @NotNull
    private String venueName;


    private String venueAddress;


    private String venuePhone;


    private String venuePassword;


    private int venueCapacity;


    private String venueDescription;


    private String venueCountry;


    private String venueState;

}
