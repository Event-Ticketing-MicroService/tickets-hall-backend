package com.ticketshall.tickets.repository;

import com.ticketshall.tickets.models.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {

    List<TicketType> getTicketTypesByEventId(UUID eventId);
}
