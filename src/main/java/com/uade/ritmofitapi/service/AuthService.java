package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.model.OTP;
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


    public void initiateLoginOrRegistration(String email) {
        String otp = generateOtp();
        OTP userOtp = new OTP(email, "deviceId");
        otpRepository.save(userOtp);

        // Asunto y cuerpo del correo
        String subject = "Tu código de acceso único";
        String body = "Hola,\n\nUsa este código para iniciar sesión en tu aplicación: " + otp + "\n\nEl código es válido por 10 minutos.";

        emailService.sendEmail(email, subject, body);
    }

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1000000));
    }

    public String validateOtp(String email, String otp) {
        // Validar OTP con mongodb.
        // En caso de que este OK y el mail no tenga user, crear user.
        // En caso de OK y el mail tiene user, devolver ese user.
        // En caso de OTP no existente, devolver error.
        return null;
    }
}