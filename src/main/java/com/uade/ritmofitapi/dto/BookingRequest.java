package com.uade.ritmofitapi.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    private String claseId;
    private LocalDateTime fechaHoraClase;

}