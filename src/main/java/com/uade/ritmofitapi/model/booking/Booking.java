package com.uade.ritmofitapi.model.booking;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "bookings")
public class Booking {
    @Id private String id;
    private String userId;
    private String classId;
    private LocalDateTime creationDate;
    private BookingStatus status;

}