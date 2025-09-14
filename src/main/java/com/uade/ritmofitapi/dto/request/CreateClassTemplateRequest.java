package com.uade.ritmofitapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateClassTemplateRequest {
    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;

    @NotBlank(message = "La disciplina no puede estar vacía")
    private String discipline;

    @NotNull(message = "La duración es requerida")
    @Positive(message = "La duración debe ser un número positivo")
    private Integer durationMinutes;

    @NotBlank(message = "El nombre del profesore no puede estar vacío")
    private String professor;

    @NotNull(message = "La capacidad es requerida")
    @Positive(message = "La capacidad debe ser un número positivo")
    private Integer capacity;
}