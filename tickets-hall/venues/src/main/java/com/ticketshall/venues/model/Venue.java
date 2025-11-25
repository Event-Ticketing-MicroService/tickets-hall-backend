package com.ticketshall.venues.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

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
    private String venuePhone;

    @Column(nullable = false)
    private String venueEmail;

    @Column(nullable = false)
    private int venueCapacity;

    @Column(nullable = false)
    private String venueDescription;

    @Column(nullable = false)
    private String venueCountry;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private VenueImage venueImage;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    private List<VenueWorker> workers = new ArrayList<>();

}
