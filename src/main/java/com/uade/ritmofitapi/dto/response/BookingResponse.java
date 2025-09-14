package com.uade.ritmofitapi.dto.response;

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