package com.uade.ritmofitapi.model.booking;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "user_bookings")
@CompoundIndex(name = "user_class_status_idx", def = "{'userId':1,'scheduledClassId':1,'status':1}")
public class UserBooking {
    @Id
    private String id;
    private String userId;
    private String scheduledClassId;
    private LocalDateTime creationDate;
    private BookingStatus status;
    private String className;
    private LocalDateTime classDateTime;
}