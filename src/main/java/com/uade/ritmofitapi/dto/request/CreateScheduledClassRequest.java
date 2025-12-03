package com.uade.ritmofitapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateScheduledClassRequest {
    @NotBlank(message = "La disciplina es obligatoria")
    private String discipline;
    
    @NotBlank(message = "El profesor es obligatorio")
    private String professor;
    
    @NotNull(message = "La duración en minutos es obligatoria")
    private Integer durationMinutes;
    
    @NotNull(message = "La capacidad es obligatoria")
    private Integer capacity;
    
    @NotBlank(message = "El ID de la sede es obligatorio")
    private String locationId;
    
    @NotBlank(message = "La fecha y hora son obligatorias (formato ISO: yyyy-MM-dd'T'HH:mm:ss)")
    private String dateTime; // Format: yyyy-MM-dd'T'HH:mm:ss
    
    private String description; // Opcional
}
