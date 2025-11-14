package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.model.OTP;
import com.uade.ritmofitapi.repository.OtpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final RateLimitService rateLimitService;

    public OtpService(OtpRepository otpRepository, EmailService emailService, RateLimitService rateLimitService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.rateLimitService = rateLimitService;
    }

    public String sendOtpForVerification(String email) {
        // Verificar rate limiting ANTES de generar OTP
        rateLimitService.checkOtpRateLimit(email);

        String otp = generateOtp();
        OTP userOtp = new OTP(email, otp);
        otpRepository.save(userOtp);

        String subject = "Verifica tu cuenta en RitmoFit";
        String body = "Hola,\n\nUsa este código para verificar tu email: " + otp + "\n\nEl código es válido por 10 minutos.";
        emailService.sendEmail(email, subject, body);
        log.info("OTP sent to {} - expires at {}", email, userOtp.getCreatedAt().plusMinutes(10));
        return userOtp.getCode();
    }

    public void validateOtp(String email, String otp) {
        OTP storedOtp = otpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP inválido o expirado."));

        // Validar expiración (10 minutos)
        LocalDateTime expirationTime = storedOtp.getCreatedAt().plusMinutes(10);
        if (LocalDateTime.now().isAfter(expirationTime)) {
            otpRepository.delete(storedOtp);
            throw new RuntimeException("El OTP ha expirado. Solicita uno nuevo.");
        }

        if (!otp.equals(storedOtp.getCode())) {
            throw new RuntimeException("El OTP es inválido.");
        }

        otpRepository.delete(storedOtp);
        // Limpiar rate limit después de verificación exitosa
        rateLimitService.clearRateLimit(email);
    }

    // Validar OTP sin eliminarlo (usado en verify-reset-otp)
    public void validateOtpWithoutDeleting(String email, String otp) {
        OTP storedOtp = otpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Código OTP inválido o expirado."));

        // Validar expiración (10 minutos)
        LocalDateTime expirationTime = storedOtp.getCreatedAt().plusMinutes(10);
        if (LocalDateTime.now().isAfter(expirationTime)) {
            otpRepository.delete(storedOtp);
            throw new RuntimeException("El OTP ha expirado. Solicita uno nuevo.");
        }

        if (!otp.equals(storedOtp.getCode())) {
            throw new RuntimeException("Código OTP inválido.");
        }
        // NO eliminamos el OTP aquí, se eliminará en resetPassword
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
