package com.ticketshall.attendance.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketshall.attendance.models.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID>{

}
