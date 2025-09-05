package com.uade.ritmofitapi.dto;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GymClassDto {
    private String id;
    private String name;
    private String discipline;
    private Integer duration;
    private Integer capacity;
    private String professor;
    private LocalDateTime dateTime;
}