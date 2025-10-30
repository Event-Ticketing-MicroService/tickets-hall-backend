package com.ticketshall.events.repositories;

import com.ticketshall.events.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("SELECT e FROM Event e JOIN FETCH e.category WHERE e.id = :id")
    Optional<Event> findByIdWithCategory(@Param("id") UUID id);
}
