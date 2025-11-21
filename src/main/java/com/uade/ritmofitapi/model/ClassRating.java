package com.uade.ritmofitapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "class_ratings")
public class ClassRating {
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    @Indexed
    private String bookingId; // Referencia a UserBooking
    
    private Integer rating; // 1-5 estrellas
    private String comment; // Comentario opcional
    private LocalDateTime createdAt;
    
    public ClassRating(String userId, String bookingId, Integer rating, String comment) {
        this.userId = userId;
        this.bookingId = bookingId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }
}
