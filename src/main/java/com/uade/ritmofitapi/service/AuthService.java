package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.model.OTP;
import com.uade.ritmofitapi.model.User;
import com.uade.ritmofitapi.repository.OtpRepository;
import com.uade.ritmofitapi.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            OtpRepository otpRepository,
            EmailService emailService,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.jwtService = jwtService;
    }


    public void initiateLoginOrRegister(String email) {
        String otp = generateOtp();
        OTP userOtp = new OTP(email, otp);

        otpRepository.save(userOtp);

        // Asunto y cuerpo del correo
        String subject = "Tu código de acceso único";
        String body = "Hola,\n\nUsa este código para iniciar sesión en tu aplicación: " + otp + "\n\nEl código es válido por 10 minutos.";

        emailService.sendEmail(email, subject, body);
    }

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1000000));
    }

    public String validateLoginWithOtp(String email, String otp) {
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

        return jwtService.generateToken(user.getId());
    }
}