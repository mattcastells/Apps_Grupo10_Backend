package com.uade.ritmofitapi.controller;

import com.sun.security.auth.UserPrincipal;
import com.uade.ritmofitapi.dto.BookingRequest;
import com.uade.ritmofitapi.dto.BookingResponse;
import com.uade.ritmofitapi.service.BookingService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingRequest bookingRequest,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        BookingResponse newBooking = bookingService.create(bookingRequest, currentUser.getName());
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<BookingResponse>> getUpcomingBookings(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<BookingResponse> upcomingBookings = bookingService.findUpcomingByUser(currentUser.getId());
        return ResponseEntity.ok(upcomingBookings);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable String bookingId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        bookingService.cancel(bookingId, currentUser.getName());
        return ResponseEntity.noContent().build(); // HTTP 204 No Content es ideal para un DELETE exitoso
    }

}