package com.uade.ritmofitapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "scheduled_classes")
public class ScheduledClass {
    @Id
    private String id;
    private String templateId;
    private String locationId;  // Denormalización para facilitar filtrado
    private String discipline;  // Denormalización para facilitar filtrado
    private LocalDateTime dateTime;
    private Integer capacity;
    private String name;
    private String professor;
    private Integer durationMinutes;
    // Cantidad de alumnos anotados para la clase
    private Integer enrolledCount = 0;

    public ScheduledClass(ClassTemplate template, LocalDateTime dateTime) {
        this.templateId = template.getId();
        this.locationId = template.getLocation() != null ? template.getLocation().getId() : null;
        this.discipline = template.getDiscipline();
        this.name = template.getName();
        this.professor = template.getProfessor();
        this.durationMinutes = template.getDurationMinutes();
        this.capacity = template.getCapacity();
        this.dateTime = dateTime;
        this.enrolledCount = 0;
    }
}
