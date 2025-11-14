package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.response.HistoryDetailResponse;
import com.uade.ritmofitapi.dto.response.HistoryItemResponse;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/me")
    public ResponseEntity<List<HistoryItemResponse>> getUserHistory(
            Authentication authentication,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        // Fix: authentication.getPrincipal() devuelve el userId (String), no el objeto User
        String userId = (String) authentication.getPrincipal();

        String fromDate = from;
        String toDate = to;
        
        if (from == null || to == null) {
            LocalDate today = LocalDate.now();
            LocalDate thirtyDaysAgo = today.minusDays(30);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            fromDate = thirtyDaysAgo.format(formatter);
            toDate = today.format(formatter);
        }

        log.info("Getting history for user {} from {} to {}", userId, fromDate, toDate);
        
        try {
            List<HistoryItemResponse> history = historyService.getUserHistory(userId, fromDate, toDate);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting user history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{attendanceId}")
    public ResponseEntity<HistoryDetailResponse> getAttendanceDetail(
            @PathVariable String attendanceId) {
        
        log.info("Getting attendance detail for {}", attendanceId);
        
        try {
            HistoryDetailResponse detail = historyService.getAttendanceDetail(attendanceId);
            return ResponseEntity.ok(detail);
        } catch (RuntimeException e) {
            log.error("Attendance not found: {}", attendanceId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting attendance detail", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
