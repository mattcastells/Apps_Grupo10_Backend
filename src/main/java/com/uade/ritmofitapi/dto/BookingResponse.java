package com.uade.ritmofitapi.dto;

import java.time.LocalDateTime;

public class BookingResponse {
    private String id;
    private String claseNombre; // Enriquecemos la respuesta
    private String sedeNombre;  // para no obligar a la app a hacer más llamadas
    private LocalDateTime fechaHoraClase;
    private String estado;

    // Getters y Setters
}