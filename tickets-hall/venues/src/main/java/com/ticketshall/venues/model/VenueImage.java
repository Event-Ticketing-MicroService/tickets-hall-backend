package com.ticketshall.venues.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long venueImageID;

    @NotNull
    private String imageURL;

    @NotNull
    private String publicId;

    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;
}
