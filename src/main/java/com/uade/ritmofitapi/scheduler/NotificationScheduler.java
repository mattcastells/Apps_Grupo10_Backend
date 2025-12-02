package com.uade.ritmofitapi.scheduler;

import com.uade.ritmofitapi.model.Notification;
import com.uade.ritmofitapi.model.booking.BookingStatus;
import com.uade.ritmofitapi.model.booking.UserBooking;
import com.uade.ritmofitapi.repository.BookingRepository;
import com.uade.ritmofitapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler que procesa notificaciones pendientes cada 15 minutos
 *
 * Este scheduler:
 * 1. Busca notificaciones con status PENDIENTE y scheduledFor <= ahora
 * 2. Las marca como ENVIADA
 * 3. Marca como ABSENT las reservas CONFIRMED que ya pasaron
 * 4. Crea notificaciones de ausencia
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final BookingRepository bookingRepository;

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

    /**
     * Se ejecuta cada 15 minutos
     * Marca como ABSENT las reservas CONFIRMED cuya clase ya finalizó
     */
    @Scheduled(cron = "0 0/15 * * * *")
    public void markAbsentBookings() {
        log.info("🔄 [SCHEDULER] Checking for absent bookings...");

        LocalDateTime now = LocalDateTime.now();

        // Buscar todas las reservas CONFIRMED
        List<UserBooking> confirmedBookings = bookingRepository.findAllByStatus(BookingStatus.CONFIRMED);

        int absentCount = 0;

        for (UserBooking booking : confirmedBookings) {
            // Calcular cuándo terminó la clase
            LocalDateTime classEnd = booking.getClassDateTime()
                    .plusMinutes(booking.getDurationMinutes() != null ? booking.getDurationMinutes() : 60);

            // Si la clase ya terminó, marcar como ABSENT
            if (now.isAfter(classEnd)) {
                try {
                    // Marcar como ABSENT
                    booking.setStatus(BookingStatus.ABSENT);
                    bookingRepository.save(booking);

                    // Crear notificación de ausencia
                    String title = "😢 Te extrañamos en tu clase";
                    String message = String.format("¿Qué pasó que no asististe a la clase de %s?",
                            booking.getClassName());

                    notificationService.createNotification(
                            booking.getUserId(),
                            Notification.NotificationType.GENERAL,
                            title,
                            message,
                            LocalDateTime.now(), // Enviar inmediatamente
                            booking.getId(),
                            booking.getScheduledClassId()
                    );

                    absentCount++;

                    log.info("❌ [SCHEDULER] Marked booking {} as ABSENT for user {} - Class: {}",
                            booking.getId(),
                            booking.getUserId(),
                            booking.getClassName()
                    );

                } catch (Exception e) {
                    log.error("❌ [SCHEDULER] Error marking booking {} as absent: {}",
                            booking.getId(),
                            e.getMessage()
                    );
                }
            }
        }

        if (absentCount > 0) {
            log.info("✅ [SCHEDULER] Marked {} bookings as ABSENT", absentCount);
        } else {
            log.info("✅ [SCHEDULER] No absent bookings found");
        }
    }
}
