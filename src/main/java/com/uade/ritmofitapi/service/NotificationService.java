package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.model.Notification;
import com.uade.ritmofitapi.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Crear una notificación pendiente
     */
    public Notification createNotification(
            String userId,
            Notification.NotificationType type,
            String title,
            String message,
            LocalDateTime scheduledFor,
            String bookingId,
            String scheduledClassId) {
        Notification notification = new Notification(userId, type, title, message, scheduledFor, bookingId,
                scheduledClassId);
        Notification saved = notificationRepository.save(notification);
        log.info("✅ Notification created for user {} - Type: {} - Scheduled for: {}", userId, type, scheduledFor);
        return saved;
    }

    /**
     * Obtener todas las notificaciones de un usuario
     */
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Obtener notificaciones enviadas (para mostrar en el drawer)
     */
    public List<Notification> getUserSentNotifications(String userId) {
        return notificationRepository.findByUserIdAndStatus(userId, Notification.NotificationStatus.ENVIADA);
    }

    /**
     * Contar notificaciones no leídas (enviadas pero no recibidas)
     */
    public long countUnreadNotifications(String userId) {
        return notificationRepository.countByUserIdAndStatus(userId, Notification.NotificationStatus.ENVIADA);
    }

    /**
     * Marcar notificación como enviada
     */
    public Notification markAsSent(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setStatus(Notification.NotificationStatus.ENVIADA);
        notification.setSentAt(LocalDateTime.now());

        log.info("📤 Notification {} marked as SENT", notificationId);
        return notificationRepository.save(notification);
    }

    /**
     * Marcar notificación como recibida (usuario la vio/clickeó/tocó)
     */
    public Notification markAsReceived(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setStatus(Notification.NotificationStatus.RECIBIDA);
        notification.setReceivedAt(LocalDateTime.now());

        log.info("✅ Notification {} marked as RECEIVED by user", notificationId);
        return notificationRepository.save(notification);
    }

    /**
     * Obtener notificaciones pendientes que deben ser procesadas
     * (scheduledFor <= ahora)
     */
    public List<Notification> getPendingNotificationsToProcess() {
        LocalDateTime now = LocalDateTime.now();
        List<Notification> pending = notificationRepository.findByStatusAndScheduledForBefore(
                Notification.NotificationStatus.PENDIENTE,
                now);

        log.info("📋 Found {} pending notifications to process", pending.size());
        return pending;
    }

    /**
     * Eliminar notificaciones de una reserva (cuando se cancela)
     */
    public void deleteNotificationsByBooking(String bookingId) {
        notificationRepository.deleteByBookingId(bookingId);
        log.info("🗑️ Deleted notifications for booking: {}", bookingId);
    }

    /**
     * Crear notificación de recordatorio 1h antes de la clase
     */
    public Notification createBookingReminder(String userId, String bookingId, String scheduledClassId,
            String className, LocalDateTime classDateTime) {
        LocalDateTime reminderTime = classDateTime.minusHours(1);

        String title = "⏰ Recordatorio de Clase";
        String message = String.format("Tu clase de %s comienza en 1 hora! 💪", className);

        return createNotification(
                userId,
                Notification.NotificationType.BOOKING_REMINDER,
                title,
                message,
                reminderTime,
                bookingId,
                scheduledClassId);
    }

    /**
     * Crear notificación de cancelación
     */
    public Notification createBookingCancellation(String userId, String bookingId, String className) {
        String title = "❌ Clase Cancelada";
        String message = String.format("Tu clase de %s ha sido cancelada.", className);

        return createNotification(
                userId,
                Notification.NotificationType.BOOKING_CANCELLED,
                title,
                message,
                LocalDateTime.now(), // Enviar inmediatamente
                bookingId,
                null // No scheduledClassId for cancellations
        );
    }

    /**
     * Crear notificación de reprogramación
     */
    public Notification createBookingRescheduled(String userId, String bookingId, String className,
            LocalDateTime newDateTime) {
        String title = "🔄 Clase Reprogramada";
        String message = String.format(
                "Tu clase de %s ha sido reprogramada para el %s a las %s",
                className,
                newDateTime.toLocalDate(),
                newDateTime.toLocalTime());

        return createNotification(
                userId,
                Notification.NotificationType.BOOKING_RESCHEDULED,
                title,
                message,
                LocalDateTime.now(), // Enviar inmediatamente
                bookingId,
                null);
    }

    /**
     * Crear notificación de cambio de clase (sede/horario)
     */
    public Notification createClassChanged(String userId, String bookingId, String className, String changeDetails) {
        String title = "📍 Cambio en tu Clase";
        String message = String.format("Tu clase de %s: %s", className, changeDetails);

        return createNotification(
                userId,
                Notification.NotificationType.CLASS_CHANGED,
                title,
                message,
                LocalDateTime.now(), // Enviar inmediatamente
                bookingId,
                null);
    }

    /**
     * Crear notificación de solicitud de calificación (después del check-in)
     */
    public Notification createRatingRequest(String userId, String bookingId, String className, String professorName,
            LocalDateTime classEndTime) {
        String title = "⭐ ¿Cómo fue tu clase?";
        String message = String.format(
                "¿Cómo fue tu clase de %s con %s? ¡Califica tu experiencia!",
                className,
                professorName);

        // Programar para 5 minutos después de que termine la clase
        LocalDateTime requestTime = classEndTime.plusMinutes(5);

        return createNotification(
                userId,
                Notification.NotificationType.REQUEST_RATING,
                title,
                message,
                requestTime,
                bookingId,
                null);
    }
}
