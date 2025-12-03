package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.request.CheckInRequest;
import com.uade.ritmofitapi.dto.response.CheckInResponse;
import com.uade.ritmofitapi.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyBooking(Authentication authentication, @RequestBody CheckInRequest request) {
        try {
            String userId = (String) authentication.getPrincipal();
            CheckInResponse response = checkInService.verifyBooking(request, userId);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "Reserva encontrada");
            successResponse.put("data", response);

            return ResponseEntity.ok(successResponse);
        } catch (RuntimeException e) {
            String errorType = e.getMessage();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", errorType);

            String message;
            switch (errorType) {
                case "NO_BOOKING_FOUND":
                    message = "No tenés una reserva para esta clase";
                    break;
                case "ALREADY_CHECKED_IN":
                    message = "Ya registraste tu asistencia a esta clase";
                    break;
                case "CLASS_EXPIRED":
                    message = "Error, la clase ya venció";
                    break;
                default:
                    message = e.getMessage();
            }

            errorResponse.put("message", message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping
    public ResponseEntity<?> checkIn(Authentication authentication, @RequestBody CheckInRequest request) {
        try {
            String userId = (String) authentication.getPrincipal();
            CheckInResponse response = checkInService.checkIn(request, userId);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "Check-in exitoso");
            successResponse.put("data", response);

            return ResponseEntity.ok(successResponse);
        } catch (RuntimeException e) {
            String errorType = e.getMessage();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", errorType);

            String message;
            switch (errorType) {
                case "NO_BOOKING_FOUND":
                    message = "No tenés una reserva para esta clase";
                    break;
                case "ALREADY_CHECKED_IN":
                    message = "Ya registraste tu asistencia a esta clase";
                    break;
                case "CLASS_EXPIRED":
                    message = "Error, la clase ya venció";
                    break;
                default:
                    message = e.getMessage();
            }

            errorResponse.put("message", message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
