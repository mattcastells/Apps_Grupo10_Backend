package com.uade.ritmofitapi.controller;

import com.uade.ritmofitapi.dto.LoginRequest;
import com.uade.ritmofitapi.model.OTP;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.repository.OtpRepository;
import com.uade.ritmofitapi.repository.UserRepository;
import com.uade.ritmofitapi.service.AuthService;
import com.uade.ritmofitapi.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

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
        authService.initiateLoginOrRegistration(request.getEmail());
        return ResponseEntity.ok("OTP enviado al correo " + request.getEmail());
    }

    // Verify OTP and get access token
    @PostMapping("/verify")
    public Map<String, String> verifyOtpAndGenerateToken(String email, String otp) {
        OTP storedOtp = otpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP inválido o expirado."));

        // Validate stored OTP code with given code
        if (!otp.equals(storedOtp.getCode())) {
            throw new RuntimeException("El OTP es invalido.");
        }

        // If OTP is correct, it is deleted from the db
        otpRepository.delete(storedOtp);

        // Find User by email and create if not exists
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User(email);
                    return userRepository.save(newUser);
                });

        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user.getId());
        return Map.of("token", accessToken);
    }

}