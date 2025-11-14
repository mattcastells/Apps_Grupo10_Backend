package com.uade.ritmofitapi.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class OtpService {

    private final EmailService emailService;

    public OtpService(EmailService emailService) {
        this.emailService = emailService;
    }

    public String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1000000));
    }

    public void sendOtpEmail(String email, String otp) {
        String subject = "Verifica tu cuenta en RitmoFit";
        String body = "Hola,\n\nUsa este código para verificar tu email: " + otp + "\n\nEl código es válido por 15 minutos.";
        emailService.sendEmail(email, subject, body);
    }

    public void sendPasswordResetEmail(String email, String otp) {
        String subject = "Recuperación de contraseña - RitmoFit";
        String body = "Hola,\n\nUsa este código para resetear tu contraseña: " + otp + "\n\nEl código es válido por 15 minutos.";
        emailService.sendEmail(email, subject, body);
    }
}
