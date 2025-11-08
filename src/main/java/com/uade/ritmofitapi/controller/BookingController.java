package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.request.BookingRequest;
import com.uade.ritmofitapi.dto.response.BookingResponse;
import com.uade.ritmofitapi.dto.response.UserBookingDto;
import com.uade.ritmofitapi.service.BookingService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest bookingRequest) {
        // Obtener userId del JWT token
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        BookingResponse newBooking = bookingService.create(bookingRequest, userId);
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<UserBookingDto>> getUserBookings() {
        // Obtener userId del JWT token
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        List<UserBookingDto> bookings = bookingService.getAllByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable String bookingId) {
        // Obtener userId del JWT token
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        bookingService.cancel(bookingId, userId);
        return ResponseEntity.noContent().build();
    }

}