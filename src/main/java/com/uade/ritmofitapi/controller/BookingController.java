package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.request.BookingRequest;
import com.uade.ritmofitapi.dto.response.BookingResponse;
import com.uade.ritmofitapi.dto.response.UserBookingDto;
import com.uade.ritmofitapi.model.User;
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
        User user = (User) authentication.getPrincipal();
        BookingResponse newBooking = bookingService.create(bookingRequest, user.getId());
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<UserBookingDto>> getUserBookings(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<UserBookingDto> bookings = bookingService.getAllByUserId(user.getId());
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(Authentication authentication, @PathVariable String bookingId) {
        User user = (User) authentication.getPrincipal();
        bookingService.cancel(bookingId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
