package com.uade.ritmofitapi.repository;

import com.uade.ritmofitapi.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findBookingByUserId(String userId);
}