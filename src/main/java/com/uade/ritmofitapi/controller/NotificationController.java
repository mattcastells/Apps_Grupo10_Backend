package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.request.MarkReadRequest;
import com.uade.ritmofitapi.dto.response.NotificationResponse;
import com.uade.ritmofitapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/mark-read")
    public ResponseEntity<Void> markAsRead(Authentication authentication, @RequestBody MarkReadRequest request) {
        String userId = (String) authentication.getPrincipal();
        notificationService.markAsRead(request.getNotificationIds(), userId);
        return ResponseEntity.noContent().build();
    }
}

