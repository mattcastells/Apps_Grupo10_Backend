package com.uade.ritmofitapi.dto.response;

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
    private String location; // Agregado para mostrar la sede en la UI
}