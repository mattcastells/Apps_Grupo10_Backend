package com.uade.ritmofitapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledClassDto {
    private String id; // Changed back to 'id' for consistency
    private String name;
    private String professor;
    private String discipline;
    private String location;
    private String locationAddress;
    private LocalDateTime dateTime;
    private Integer durationMinutes;
    private Integer availableSlots;
    private Integer capacity;
    private String description;
}