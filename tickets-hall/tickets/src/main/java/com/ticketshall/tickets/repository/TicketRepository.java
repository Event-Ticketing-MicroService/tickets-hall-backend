package com.ticketshall.tickets.repository;

import com.ticketshall.tickets.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
}
