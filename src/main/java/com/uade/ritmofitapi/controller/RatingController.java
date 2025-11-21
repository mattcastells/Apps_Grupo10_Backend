package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.request.CreateRatingRequest;
import com.uade.ritmofitapi.dto.response.RatingResponse;
import com.uade.ritmofitapi.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<RatingResponse> createRating(
            Authentication authentication,
            @PathVariable String bookingId,
            @Valid @RequestBody CreateRatingRequest request) {
        
        String userId = (String) authentication.getPrincipal();
        
        try {
            RatingResponse rating = ratingService.createRating(userId, bookingId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(rating);
        } catch (RuntimeException e) {
            log.error("Error creating rating: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<RatingResponse> getRatingByBookingId(@PathVariable String bookingId) {
        RatingResponse rating = ratingService.getRatingByBookingId(bookingId);
        
        if (rating == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(rating);
    }
}

