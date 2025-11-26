package com.ticketshall.attendance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketshall.attendance.services.AttendanceService;

@RestController

@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/worker/{ticketCode}")
    public ResponseEntity<Object> attendTicket(@PathVariable String ticketCode) {
        attendanceService.useTicket(ticketCode);
        return ResponseEntity.ok().build();
    }
}
