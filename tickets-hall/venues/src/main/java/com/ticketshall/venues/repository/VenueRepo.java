package com.ticketshall.venues.repository;

import com.ticketshall.venues.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VenueRepo extends JpaRepository<Venue, Long> {

}
