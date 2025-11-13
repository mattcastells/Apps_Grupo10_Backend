package com.uade.ritmofitapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@Document(collection = "class_templates")
public class ClassTemplate {
    @Id
    private String id;
    private String name;
    private String discipline;
    private Integer durationMinutes;
    private Integer capacity;
    private String professor;
    private DayOfWeek dayOfWeek;
    private LocalTime time;

    @DBRef
    private Location location; // Cada pertenece a una sede

    public ClassTemplate(String name, String discipline, String professor, Integer durationMinutes, Integer capacity, DayOfWeek dayOfWeek, LocalTime time, Location location) {
        this.name = name;
        this.discipline = discipline;
        this.professor = professor;
        this.durationMinutes = durationMinutes;
        this.capacity = capacity;
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.location = location;
    }
}
