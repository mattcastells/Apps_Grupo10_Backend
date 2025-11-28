package com.uade.ritmofitapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "notifications")
@CompoundIndex(name = "user_read_idx", def = "{'userId':1,'read':1,'createdAt':-1}")
public class Notification {
    @Id
    private String id;
    private String userId;
    private NotificationType type;
    private String title;
    private String body;
    private boolean read = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Datos adicionales para navegación y acciones
    private String bookingId;
    private String scheduledClassId;
    private String actionUrl; // Para acciones como aceptar/rechazar cambios
    
    // Para cambios de horario
    private LocalDateTime oldDateTime;
    private LocalDateTime newDateTime;
    
    public enum NotificationType {
        REMINDER,           // Recordatorio 1h antes
        CLASS_CANCELLED,    // Clase cancelada
        CLASS_RESCHEDULED,  // Clase reprogramada
        CLASS_CHANGE        // Cambio genérico
    }
}

