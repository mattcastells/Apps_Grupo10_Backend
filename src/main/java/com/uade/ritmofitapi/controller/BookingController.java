package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.request.BookingRequest;
import com.uade.ritmofitapi.dto.response.BookingResponse;
import com.uade.ritmofitapi.dto.response.UserBookingDto;
import com.uade.ritmofitapi.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(Authentication authentication, @RequestBody BookingRequest bookingRequest) {
        String userId = (String) authentication.getPrincipal();
        BookingResponse newBooking = bookingService.create(bookingRequest, userId);
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<UserBookingDto>> getUserBookings(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        List<UserBookingDto> bookings = bookingService.getAllByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/history")
    public ResponseEntity<List<UserBookingDto>> getUserBookingHistory(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        List<UserBookingDto> bookings = bookingService.getAllBookingsHistory(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/booked-class-ids")
    public ResponseEntity<List<String>> getBookedClassIds(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        List<String> bookedClassIds = bookingService.getBookedClassIds(userId);
        return ResponseEntity.ok(bookedClassIds);
    }


    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(Authentication authentication, @PathVariable String bookingId) {
        String userId = (String) authentication.getPrincipal();
        bookingService.cancel(bookingId, userId);
        return ResponseEntity.noContent().build();
    }
}
