package com.uade.ritmofitapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassTemplateDto {
    private String id;
    private String name;
    private String discipline;
    private Integer duration;
    private Integer capacity;
}