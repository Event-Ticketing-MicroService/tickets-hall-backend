package com.ticketshall.tickets.repository;

import com.ticketshall.tickets.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
}
