package com.ticketshall.attendance.controller;

import com.ticketshall.attendance.error.exceptions.EventAlreadyEndedException;
import com.ticketshall.attendance.error.exceptions.TicketAlreadyUsedException;
import com.ticketshall.attendance.error.exceptions.TicketNotFoundException;
import com.ticketshall.attendance.services.AttendanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @Test
    void attendTicket_WithValidTicket_ShouldReturn200() throws Exception {
        // Arrange
        String ticketCode = "VALID-TICKET-123";
        doNothing().when(attendanceService).useTicket(ticketCode);

        // Act & Assert
        mockMvc.perform(post("/attendances/{ticketCode}", ticketCode))
                .andExpect(status().isOk());

        verify(attendanceService, times(1)).useTicket(ticketCode);
    }

    @Test
    void attendTicket_WithNonExistentTicket_ShouldReturn404() throws Exception {
        // Arrange
        String ticketCode = "NON-EXISTENT";
        doThrow(new TicketNotFoundException("Ticket with code 'NON-EXISTENT' not found"))
                .when(attendanceService).useTicket(ticketCode);

        // Act & Assert
        mockMvc.perform(post("/attendances/{ticketCode}", ticketCode))
                .andExpect(status().isNotFound());

        verify(attendanceService, times(1)).useTicket(ticketCode);
    }

    @Test
    void attendTicket_WithAlreadyUsedTicket_ShouldReturn409() throws Exception {
        // Arrange
        String ticketCode = "USED-TICKET";
        doThrow(new TicketAlreadyUsedException("Ticket with code 'USED-TICKET' has already been used"))
                .when(attendanceService).useTicket(ticketCode);

        // Act & Assert
        mockMvc.perform(post("/attendances/{ticketCode}", ticketCode))
                .andExpect(status().isConflict());

        verify(attendanceService, times(1)).useTicket(ticketCode);
    }

    @Test
    void attendTicket_WithEndedEvent_ShouldReturn409() throws Exception {
        // Arrange
        String ticketCode = "PAST-EVENT-TICKET";
        doThrow(new EventAlreadyEndedException("Event has already ended"))
                .when(attendanceService).useTicket(ticketCode);

        // Act & Assert
        mockMvc.perform(post("/attendances/{ticketCode}", ticketCode))
                .andExpect(status().isConflict());

        verify(attendanceService, times(1)).useTicket(ticketCode);
    }

    @Test
    void attendTicket_WithSpecialCharactersInCode_ShouldHandleCorrectly() throws Exception {
        // Arrange
        String ticketCode = "TICKET-WITH-SPECIAL-CHARS-!@#";
        doNothing().when(attendanceService).useTicket(ticketCode);

        // Act & Assert
        mockMvc.perform(post("/attendances/{ticketCode}", ticketCode))
                .andExpect(status().isOk());

        verify(attendanceService, times(1)).useTicket(ticketCode);
    }
}
