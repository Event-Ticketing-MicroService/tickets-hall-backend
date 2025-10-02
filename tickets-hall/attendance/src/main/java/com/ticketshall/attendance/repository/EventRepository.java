package com.ticketshall.attendance.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketshall.attendance.models.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

}
