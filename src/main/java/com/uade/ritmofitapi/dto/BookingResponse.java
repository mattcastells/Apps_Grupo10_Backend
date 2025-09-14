package com.uade.ritmofitapi.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private String id;
    private String className;
    private String locationName;
    private LocalDateTime classDateTime;
    private String status;
}