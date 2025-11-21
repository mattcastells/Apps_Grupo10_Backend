package com.uade.ritmofitapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRatingRequest {
    @NotNull(message = "El rating es obligatorio")
    @Min(value = 1, message = "El rating debe ser al menos 1")
    @Max(value = 5, message = "El rating debe ser máximo 5")
    private Integer rating;
    
    private String comment; // Opcional
}

