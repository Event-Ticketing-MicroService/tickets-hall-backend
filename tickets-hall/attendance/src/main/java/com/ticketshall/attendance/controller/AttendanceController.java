package com.ticketshall.attendance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketshall.attendance.services.AttendanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/{ticketCode}")
    public ResponseEntity<Object> attendTicket(@PathVariable String ticketCode)
    {
        attendanceService.useTicket(ticketCode);
        return ResponseEntity.ok().build();
    }
}
