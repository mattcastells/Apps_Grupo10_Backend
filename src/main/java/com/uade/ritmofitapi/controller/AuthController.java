package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.LoginRequest;
import com.uade.ritmofitapi.dto.VerifyOtpRequest;
import com.uade.ritmofitapi.repository.OtpRepository;
import com.uade.ritmofitapi.repository.UserRepository;
import com.uade.ritmofitapi.service.AuthService;
import com.uade.ritmofitapi.service.JwtService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(AuthService authService, OtpRepository otpRepository, UserRepository userRepository, JwtService jwtService) {
        this.authService = authService;
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // Generate OTP for login
    @PostMapping("/login")
    public ResponseEntity<String> loginOrRegister(@Valid @RequestBody LoginRequest request) {
        log.info("Iniciando autenticacion de usuario");
        authService.initiateLoginOrRegister(request.getEmail());
        return ResponseEntity.ok("OTP enviado al correo " + request.getEmail());
    }

    // Verify OTP and get access token
    @PostMapping("/verify")
    public Map<String, String> verifyOtpAndGenerateToken(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("Iniciando validacion de OTP para {}", request.getEmail());
        String accessToken = authService.validateLoginWithOtp(request.getEmail(), request.getOtp());
        return Map.of("token", accessToken);
    }

}