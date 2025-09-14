package com.uade.ritmofitapi.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
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
}