package com.uade.ritmofitapi.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "classes")
public class GymClass {
    @Id private String id;
    private String name;
    private String discipline;
    private Integer duration;
    private Integer capacity;
    private String professor;
    private LocalDateTime dateTime;
}
 