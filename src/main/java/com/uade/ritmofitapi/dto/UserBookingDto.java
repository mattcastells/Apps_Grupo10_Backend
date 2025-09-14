package com.uade.ritmofitapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBookingDto {
    private String bookingId;
    private String className;
    private LocalDateTime classDateTime;
    private String professor;
    private String status;
}