package com.ticketshall.attendance.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketshall.attendance.services.AttendanceService;

import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequestMapping("/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/{ticketCode}")
    public ResponseEntity<Object> attendTicket(@PathVariable String ticketCode) {
        log.debug(String.format("get attendance ticket code %s", ticketCode));
        attendanceService.useTicket(ticketCode);
        return ResponseEntity.ok().build();
    }
}
