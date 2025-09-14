package com.uade.ritmofitapi.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "scheduled_classes")
public class ScheduledClass {
    @Id
    private String id;
    private String templateId;
    private LocalDateTime dateTime;
    private Integer capacity;
    private String name;
    private String professor;
    private Integer durationMinutes;
    // Cantidad de alumnos anotados para la clase
    private Integer enrolledCount = 0;
}