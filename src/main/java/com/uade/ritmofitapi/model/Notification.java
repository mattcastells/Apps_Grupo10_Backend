package com.uade.ritmofitapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Modelo de Notificación para el sistema de notificaciones push
 *
 * Estados:
 * - PENDIENTE: Notificación creada, esperando a ser enviada
 * - ENVIADA: Notificación enviada al dispositivo del usuario
 * - RECIBIDA: Usuario interactuó con la notificación (click en OK o abrió)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    /**
     * ID del usuario destinatario
     */
    private String userId;

    /**
     * Tipo de notificación
     * Valores posibles: BOOKING_REMINDER, BOOKING_CANCELLED, BOOKING_RESCHEDULED
     */
    private NotificationType type;

    /**
     * Título de la notificación
     */
    private String title;

    /**
     * Mensaje/cuerpo de la notificación
     */
    private String message;

    /**
     * Estado de la notificación
     */
    private NotificationStatus status;

    /**
     * ID de la reserva relacionada (opcional)
     */
    private String bookingId;

    /**
     * Fecha y hora programada para enviar la notificación
     */
    private LocalDateTime scheduledFor;

    /**
     * Fecha y hora de creación
     */
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de envío (cuando status = ENVIADA)
     */
    private LocalDateTime sentAt;

    /**
     * Fecha y hora de lectura (cuando status = LEIDA)
     */
    private LocalDateTime readAt;

    /**
     * Datos adicionales en formato JSON (opcional)
     */
    private String metadata;

    public Notification(String userId, NotificationType type, String title, String message, LocalDateTime scheduledFor, String bookingId) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.scheduledFor = scheduledFor;
        this.bookingId = bookingId;
        this.status = NotificationStatus.PENDIENTE;
        this.createdAt = LocalDateTime.now();
    }

    public enum NotificationType {
        BOOKING_REMINDER,       // Recordatorio 1h antes de la clase
        BOOKING_CANCELLED,      // Clase cancelada
        BOOKING_RESCHEDULED,    // Clase reprogramada
        CLASS_CHANGED,          // Clase cambió sede/horario
        REQUEST_RATING,         // Solicitud de calificación después de check-in
        GENERAL                 // Notificación general
    }

    public enum NotificationStatus {
        PENDIENTE,  // Creada, esperando ser enviada
        ENVIADA,    // Mostrada al usuario
        LEIDA       // Usuario la leyó/tocó
    }
}
