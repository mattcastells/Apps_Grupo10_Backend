package com.uade.ritmofitapi.repository;

import com.uade.ritmofitapi.model.ClassRating;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ClassRatingRepository extends MongoRepository<ClassRating, String> {
    Optional<ClassRating> findByBookingId(String bookingId);
    boolean existsByBookingId(String bookingId);
}

