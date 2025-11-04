package com.ticketshall.venues.repository;

import com.ticketshall.venues.model.VenueImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VenueImgRepo extends JpaRepository<VenueImage , Long> {
}
