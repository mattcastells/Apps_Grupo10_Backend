package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> requestOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.requestOtp(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> validateOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String token = authService.validateOtp(email, otp);
        return ResponseEntity.ok(Map.of("token", token));
    }
}