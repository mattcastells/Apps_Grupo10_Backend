package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.model.booking.UserBooking;
import com.uade.ritmofitapi.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final BookingRepository bookingRepository;

    @GetMapping("/all-bookings")
    public ResponseEntity<List<UserBooking>> getAllBookings() {
        List<UserBooking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/booking-info")
    public ResponseEntity<Map<String, Object>> getBookingInfo() {
        List<UserBooking> allBookings = bookingRepository.findAll();

        Map<String, Object> response = new HashMap<>();
        response.put("totalBookings", allBookings.size());
        response.put("bookings", allBookings);

        return ResponseEntity.ok(response);
    }
}
