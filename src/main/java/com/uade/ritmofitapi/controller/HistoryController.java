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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/user")
    public ResponseEntity<List<HistoryItemResponse>> getUserHistory(
            Authentication authentication,
            @RequestParam String from,
            @RequestParam String to) {
        User user = (User) authentication.getPrincipal();
        String userId = user.getId();
        log.info("Getting history for user {} from {} to {}", userId, from, to);
        
        try {
            List<HistoryItemResponse> history = historyService.getUserHistory(userId, from, to);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting user history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/attendances/{attendanceId}")
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
