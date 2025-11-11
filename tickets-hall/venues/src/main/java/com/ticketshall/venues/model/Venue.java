package com.ticketshall.venues.model;

import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long venueID;

    @Column(nullable = false)
    private String venueName;

    @Column(nullable = false)
    private String venueAddress;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private String venuePhone;

    @Column(nullable = false)
    private String venueEmail;

    @Column(nullable = false)
    private int venueCapacity;

    @Column(nullable = false)
    private String venueDescription;

    @Column(nullable = false)
    private String venueCountry;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    private List<VenueImage> venueImages = new ArrayList<>();

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    private List<VenueWorker> workers = new ArrayList<>();

}
