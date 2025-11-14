package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.request.ForgotPasswordRequest;
import com.uade.ritmofitapi.dto.request.LoginRequest;
import com.uade.ritmofitapi.dto.request.RegisterRequest;
import com.uade.ritmofitapi.dto.request.ResetPasswordRequest;
import com.uade.ritmofitapi.dto.request.VerifyOtpRequest;
import com.uade.ritmofitapi.dto.request.VerifyResetOtpRequest;
import com.uade.ritmofitapi.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
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
        log.info("SUCCESSFUL LOGIN FOR USER: " + request.getEmail());
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

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Código OTP enviado a tu email."));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<Map<String, String>> verifyResetOtp(@RequestBody VerifyResetOtpRequest request) {
        authService.verifyResetOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", "OTP verificado correctamente."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña restablecida exitosamente."));
    }
}
