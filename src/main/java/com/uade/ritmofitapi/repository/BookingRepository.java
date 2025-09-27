package com.uade.ritmofitapi.repository;

import com.uade.ritmofitapi.model.booking.UserBooking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<UserBooking, String> {
    List<UserBooking> findAllByUserId(String userId);
    Boolean existsByUserIdAndScheduledClassId(String userId, String scheduledClassId);
    List<UserBooking> findByUserIdAndClassDateTimeBetween(String userId, LocalDateTime start, LocalDateTime end);
}