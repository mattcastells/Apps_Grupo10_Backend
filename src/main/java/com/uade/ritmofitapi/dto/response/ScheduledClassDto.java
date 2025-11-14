package com.uade.ritmofitapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledClassDto {
    private String id;
    private String name;
    private String professor;
    private LocalDateTime dateTime;
    private Integer durationMinutes;
    private Integer availableSlots;
    private String location; // Nombre de la sede
}