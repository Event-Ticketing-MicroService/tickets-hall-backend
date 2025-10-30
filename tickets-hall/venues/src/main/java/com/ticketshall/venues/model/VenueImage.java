package com.ticketshall.venues.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
public class VenueImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO )
    private Long venueImageID;

    @NotNull
    private String imageURL;

    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;
}
