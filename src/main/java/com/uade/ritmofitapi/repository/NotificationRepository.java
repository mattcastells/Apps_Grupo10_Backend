package com.uade.ritmofitapi.repository;

import com.uade.ritmofitapi.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    /**
     * Buscar todas las notificaciones de un usuario ordenadas por fecha de creación (más recientes primero)
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Buscar notificaciones por usuario y estado
     */
    List<Notification> findByUserIdAndStatus(String userId, Notification.NotificationStatus status);

    /**
     * Contar notificaciones enviadas (no recibidas) para un usuario
     * Útil para el badge contador
     */
    long countByUserIdAndStatus(String userId, Notification.NotificationStatus status);

    /**
     * Buscar notificaciones pendientes que deben ser enviadas (scheduledFor <= ahora)
     */
    List<Notification> findByStatusAndScheduledForBefore(
            Notification.NotificationStatus status,
            LocalDateTime dateTime
    );

    /**
     * Buscar notificaciones por bookingId
     */
    List<Notification> findByBookingId(String bookingId);

    /**
     * Eliminar notificaciones de una reserva específica
     */
    void deleteByBookingId(String bookingId);
}
