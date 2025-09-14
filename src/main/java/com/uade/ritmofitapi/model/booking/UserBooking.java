package com.uade.ritmofitapi.model.booking;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@Document(collection = "user_bookings")
public class UserBooking {
    @Id
    private String id;
    private String userId;
    private String scheduledClassId;
    private LocalDateTime creationDate;
    private BookingStatus status;
    private String className; // Genera redundancia pero acelera la query
    private LocalDateTime classDateTime;
}