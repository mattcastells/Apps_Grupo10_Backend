package com.uade.ritmofitapi.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateScheduledClassRequest {
    private LocalDateTime newDateTime;
    private String newLocationId;
    private String newLocation;  // nombre de la location
    private String reason;  // razón del cambio (opcional)
}
