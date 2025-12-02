package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.CreateNotificationRequest;
import com.uade.ritmofitapi.model.Notification;
import com.uade.ritmofitapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * POST /api/v1/notifications
     * Crear una nueva notificación
     */
    @PostMapping
    public ResponseEntity<Notification> createNotification(
            @RequestBody CreateNotificationRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        Notification notification = notificationService.createNotification(
                userId,
                request.getType(),
                request.getTitle(),
                request.getMessage(),
                request.getScheduledFor(),
                request.getBookingId(),
                request.getScheduledClassId());
        return ResponseEntity.ok(notification);
    }

    /**
     * GET /api/v1/notifications
     * Obtener todas las notificaciones del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(Authentication authentication) {
        String userId = authentication.getName();
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/v1/notifications/pending
     * Obtener notificaciones PENDIENTES listas para enviar (scheduledFor <= now)
     * El worker del frontend llama a este endpoint cada 15 mins
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Notification>> getPendingNotifications(Authentication authentication) {
        String userId = authentication.getName();
        List<Notification> pending = notificationService.getPendingNotificationsToProcess();

        // Filtrar solo las del usuario autenticado
        List<Notification> userPending = pending.stream()
                .filter(n -> n.getUserId().equals(userId))
                .toList();

        // Marcar como ENVIADA
        userPending.forEach(n -> notificationService.markAsSent(n.getId()));

        log.info("📬 Returning {} pending notifications for user {}", userPending.size(), userId);
        return ResponseEntity.ok(userPending);
    }

    /**
     * GET /api/v1/notifications/unread-count
     * Obtener cantidad de notificaciones no leídas (para el badge)
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        String userId = authentication.getName();
        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * PUT /api/v1/notifications/{id}/received
     * Marcar notificación como recibida (usuario la vio/interactuó)
     */
    @PutMapping("/{id}/received")
    public ResponseEntity<Notification> markAsReceived(
            @PathVariable String id,
            Authentication authentication) {
        // TODO: Verificar que la notificación pertenece al usuario autenticado
        Notification updated = notificationService.markAsReceived(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * GET /api/v1/notifications/sent
     * Obtener solo notificaciones enviadas (para mostrar en el drawer)
     */
    @GetMapping("/sent")
    public ResponseEntity<List<Notification>> getSentNotifications(Authentication authentication) {
        String userId = authentication.getName();
        List<Notification> notifications = notificationService.getUserSentNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
}
