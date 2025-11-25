package com.ticketshall.attendance.services.impl;

import java.time.LocalDateTime;

import com.ticketshall.attendance.services.AttendanceService;
import org.springframework.stereotype.Service;

import com.ticketshall.attendance.error.exceptions.EventAlreadyEndedException;
import com.ticketshall.attendance.error.exceptions.TicketAlreadyUsedException;
import com.ticketshall.attendance.error.exceptions.TicketNotFoundException;
import com.ticketshall.attendance.entity.Ticket;
import com.ticketshall.attendance.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final TicketRepository ticketRepository;

    @Override
    public void useTicket(String ticketCode) {
        Ticket ticket = ticketRepository.findByCode(ticketCode)
                            .orElseThrow(()-> new TicketNotFoundException("Ticket with code '" + ticketCode + "' not found"));

        if(ticket.getUsedAtUtc() != null)
        {
            throw new TicketAlreadyUsedException("Ticket with code '" + ticketCode + "' has already been used");
        }

        if(ticket.getEvent().getEndsAtUtc().isBefore(LocalDateTime.now()))
        {
            throw new EventAlreadyEndedException("Event has already ended");
        }

        ticket.setUsedAtUtc(LocalDateTime.now());
        
        ticketRepository.save(ticket);
    }


}
