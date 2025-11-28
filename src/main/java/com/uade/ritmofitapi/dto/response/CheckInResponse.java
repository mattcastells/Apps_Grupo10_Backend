package com.uade.ritmofitapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponse {
    private String bookingId;
    private String className;
    private LocalDateTime classDateTime;
    private String location;
    private String professor;
    private Integer durationMinutes;
    private String status;
}
