package com.uade.ritmofitapi.scheduler;

import com.uade.ritmofitapi.model.Notification;
import com.uade.ritmofitapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduler que procesa notificaciones pendientes cada 15 minutos
 *
 * Este scheduler:
 * 1. Busca notificaciones con status PENDIENTE y scheduledFor <= ahora
 * 2. Las marca como ENVIADA
 * 3. En un futuro, aquí se integraría con un servicio de push (Firebase, OneSignal, etc)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * Se ejecuta cada 15 minutos
     * Cron: segundo minuto hora día mes día-semana
     * 0 0/15 * * * * = cada 15 minutos
     */
    @Scheduled(cron = "0 0/15 * * * *")
    public void processPendingNotifications() {
        log.info("🔄 [SCHEDULER] Processing pending notifications...");

        List<Notification> pendingNotifications = notificationService.getPendingNotificationsToProcess();

        if (pendingNotifications.isEmpty()) {
            log.info("✅ [SCHEDULER] No pending notifications to process");
            return;
        }

        log.info("📬 [SCHEDULER] Found {} notifications to send", pendingNotifications.size());

        for (Notification notification : pendingNotifications) {
            try {
                // Marcar como enviada
                notificationService.markAsSent(notification.getId());

                log.info("📤 [SCHEDULER] Sent notification to user {}: {} - {}",
                        notification.getUserId(),
                        notification.getTitle(),
                        notification.getMessage()
                );

                // TODO: Aquí se integraría con un servicio de push notifications
                // Por ejemplo: Firebase Cloud Messaging, OneSignal, etc.
                // pushService.sendPushNotification(notification);

            } catch (Exception e) {
                log.error("❌ [SCHEDULER] Error processing notification {}: {}",
                        notification.getId(),
                        e.getMessage()
                );
            }
        }

        log.info("✅ [SCHEDULER] Finished processing notifications");
    }

    /**
     * Método manual para testing - ejecutar bajo demanda
     */
    public void processPendingNotificationsNow() {
        log.info("🔧 [MANUAL] Manually processing pending notifications...");
        processPendingNotifications();
    }
}
