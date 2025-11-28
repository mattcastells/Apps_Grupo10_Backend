package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.dto.response.NotificationResponse;
import com.uade.ritmofitapi.model.Notification;
import com.uade.ritmofitapi.model.ScheduledClass;
import com.uade.ritmofitapi.model.booking.BookingStatus;
import com.uade.ritmofitapi.model.booking.UserBooking;
import com.uade.ritmofitapi.repository.BookingRepository;
import com.uade.ritmofitapi.repository.NotificationRepository;
import com.uade.ritmofitapi.repository.ScheduledClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final BookingRepository bookingRepository;
    private final ScheduledClassRepository scheduledClassRepository;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Obtener notificaciones pendientes (no leídas) para un usuario
     */
    public List<NotificationResponse> getUnreadNotifications(String userId) {
        log.info("Obteniendo notificaciones no leídas para usuario: {}", userId);
        List<Notification> notifications = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Marcar notificaciones como leídas
     */
    @Transactional
    public void markAsRead(List<String> notificationIds, String userId) {
        log.info("Marcando {} notificaciones como leídas para usuario: {}", notificationIds.size(), userId);
        for (String notificationId : notificationIds) {
            notificationRepository.findById(notificationId)
                    .ifPresent(notification -> {
                        if (notification.getUserId().equals(userId)) {
                            notification.setRead(true);
                            notificationRepository.save(notification);
                        }
                    });
        }
    }

    /**
     * Crear notificación de recordatorio 1h antes de la clase
     */
    public void createReminderNotification(UserBooking booking, ScheduledClass scheduledClass) {
        log.info("Creando notificación de recordatorio para booking: {}", booking.getId());
        
        // Verificar si ya existe una notificación de recordatorio para esta reserva
        boolean exists = notificationRepository.findByUserIdOrderByCreatedAtDesc(booking.getUserId())
                .stream()
                .anyMatch(n -> n.getType() == Notification.NotificationType.REMINDER 
                        && n.getBookingId() != null 
                        && n.getBookingId().equals(booking.getId())
                        && !n.isRead());
        
        if (exists) {
            log.info("Ya existe una notificación de recordatorio para booking: {}", booking.getId());
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(booking.getUserId());
        notification.setType(Notification.NotificationType.REMINDER);
        notification.setTitle("Recordatorio de Clase");
        notification.setBody(String.format(
                "Tu clase de %s es en 1 hora. Horario: %s",
                scheduledClass.getName(),
                scheduledClass.getDateTime().format(dateTimeFormatter)
        ));
        notification.setBookingId(booking.getId());
        notification.setScheduledClassId(scheduledClass.getId());
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
        log.info("Notificación de recordatorio creada: {}", notification.getId());
    }

    /**
     * Crear notificación cuando se cancela una clase
     */
    public void createClassCancelledNotification(UserBooking booking, ScheduledClass scheduledClass) {
        log.info("Creando notificación de clase cancelada para booking: {}", booking.getId());
        
        Notification notification = new Notification();
        notification.setUserId(booking.getUserId());
        notification.setType(Notification.NotificationType.CLASS_CANCELLED);
        notification.setTitle("Clase Cancelada");
        notification.setBody(String.format(
                "La clase de %s programada para el %s ha sido cancelada.",
                scheduledClass.getName(),
                scheduledClass.getDateTime().format(dateTimeFormatter)
        ));
        notification.setBookingId(booking.getId());
        notification.setScheduledClassId(scheduledClass.getId());
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
        log.info("Notificación de clase cancelada creada: {}", notification.getId());
    }

    /**
     * Crear notificación cuando se reprograma una clase
     */
    public void createClassRescheduledNotification(UserBooking booking, ScheduledClass scheduledClass, 
                                                   LocalDateTime oldDateTime, LocalDateTime newDateTime) {
        log.info("Creando notificación de clase reprogramada para booking: {}", booking.getId());
        
        Notification notification = new Notification();
        notification.setUserId(booking.getUserId());
        notification.setType(Notification.NotificationType.CLASS_RESCHEDULED);
        notification.setTitle("Clase Reprogramada");
        notification.setBody(String.format(
                "La clase de %s ha sido reprogramada. Nuevo horario: %s",
                scheduledClass.getName(),
                newDateTime.format(dateTimeFormatter)
        ));
        notification.setBookingId(booking.getId());
        notification.setScheduledClassId(scheduledClass.getId());
        notification.setOldDateTime(oldDateTime);
        notification.setNewDateTime(newDateTime);
        notification.setActionUrl("/api/v1/booking/accept-reschedule/" + booking.getId());
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
        log.info("Notificación de clase reprogramada creada: {}", notification.getId());
    }

    /**
     * Tarea programada que se ejecuta cada minuto para generar recordatorios
     * 1 hora antes de las clases
     */
    @Scheduled(fixedRate = 60000) // Cada minuto
    public void generateReminderNotifications() {
        log.debug("Ejecutando tarea programada para generar recordatorios");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourFromNow = now.plusHours(1);
        LocalDateTime oneHourOneMinuteFromNow = oneHourFromNow.plusMinutes(1);
        
        // Buscar reservas confirmadas que empiecen entre 1h y 1h1min desde ahora
        List<UserBooking> upcomingBookings = bookingRepository.findAll()
                .stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .filter(booking -> {
                    LocalDateTime classTime = booking.getClassDateTime();
                    return classTime.isAfter(oneHourFromNow) && classTime.isBefore(oneHourOneMinuteFromNow);
                })
                .collect(Collectors.toList());
        
        log.info("Encontradas {} reservas que necesitan recordatorio", upcomingBookings.size());
        
        for (UserBooking booking : upcomingBookings) {
            scheduledClassRepository.findById(booking.getScheduledClassId())
                    .ifPresent(scheduledClass -> {
                        createReminderNotification(booking, scheduledClass);
                    });
        }
    }

    /**
     * Notificar a todos los usuarios con reservas cuando se cancela una clase
     */
    public void notifyClassCancellation(String scheduledClassId) {
        log.info("Notificando cancelación de clase: {}", scheduledClassId);
        
        ScheduledClass scheduledClass = scheduledClassRepository.findById(scheduledClassId)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada: " + scheduledClassId));
        
        List<UserBooking> bookings = bookingRepository.findAll()
                .stream()
                .filter(booking -> booking.getScheduledClassId().equals(scheduledClassId))
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .collect(Collectors.toList());
        
        for (UserBooking booking : bookings) {
            createClassCancelledNotification(booking, scheduledClass);
        }
    }

    /**
     * Notificar a todos los usuarios con reservas cuando se reprograma una clase
     */
    public void notifyClassReschedule(String scheduledClassId, LocalDateTime oldDateTime, LocalDateTime newDateTime) {
        log.info("Notificando reprogramación de clase: {}", scheduledClassId);
        
        ScheduledClass scheduledClass = scheduledClassRepository.findById(scheduledClassId)
                .orElseThrow(() -> new RuntimeException("Clase no encontrada: " + scheduledClassId));
        
        List<UserBooking> bookings = bookingRepository.findAll()
                .stream()
                .filter(booking -> booking.getScheduledClassId().equals(scheduledClassId))
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .collect(Collectors.toList());
        
        for (UserBooking booking : bookings) {
            createClassRescheduledNotification(booking, scheduledClass, oldDateTime, newDateTime);
        }
    }
}

