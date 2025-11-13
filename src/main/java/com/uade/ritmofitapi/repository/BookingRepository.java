package com.uade.ritmofitapi.repository;

import com.uade.ritmofitapi.model.booking.UserBooking;
import com.uade.ritmofitapi.model.booking.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends MongoRepository<UserBooking, String> {
    List<UserBooking> findAllByUserId(String userId);
    Boolean existsByUserIdAndScheduledClassId(String userId, String scheduledClassId);
    Boolean existsByUserIdAndScheduledClassIdAndStatus(String userId, String scheduledClassId, BookingStatus status); // NUEVO
    List<UserBooking> findByUserIdAndClassDateTimeBetween(String userId, LocalDateTime start, LocalDateTime end);
    Optional<UserBooking> findByUserIdAndScheduledClassId(String id, String id1);
}