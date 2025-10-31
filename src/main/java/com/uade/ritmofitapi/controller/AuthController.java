package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.request.LoginRequest;
import com.uade.ritmofitapi.dto.request.VerifyOtpRequest;
import com.uade.ritmofitapi.repository.OtpRepository;
import com.uade.ritmofitapi.repository.UserRepository;
import com.uade.ritmofitapi.service.AuthService;
import com.uade.ritmofitapi.service.JwtService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uade.ritmofitapi.dto.request.LoginRequest; // Necesitarás un DTO para el login
import com.uade.ritmofitapi.dto.request.RegisterRequest; // Y otro para el registro
import com.uade.ritmofitapi.dto.request.VerifyOtpRequest;
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

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        authService.register(request.getName(), request.getEmail(), request.getPassword(), request.getAge(), request.getGender());
        return ResponseEntity.ok(Map.of("message", "Usuario registrado. Por favor, verifica tu email con el OTP enviado."));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody VerifyOtpRequest request) {
        String token = authService.verifyEmail(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of(
            "message", "Email verificado correctamente.",
            "token", token
        ));
    }
}