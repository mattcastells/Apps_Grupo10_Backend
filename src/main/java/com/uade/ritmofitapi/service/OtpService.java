// Archivo: com.uade.ritmofitapi.service.OtpService.java
package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.model.OTP;
import com.uade.ritmofitapi.repository.OtpRepository;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;

    public OtpService(OtpRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    public String sendOtpForVerification(String email) {
        String otp = generateOtp();
        OTP userOtp = new OTP(email, otp);
        otpRepository.save(userOtp);

        String subject = "Verifica tu cuenta en RitmoFit";
        String body = "Hola,\n\nUsa este código para verificar tu email: " + otp + "\n\nEl código es válido por 10 minutos.";
        emailService.sendEmail(email, subject, body);
        return userOtp.getCode();
    }

    public void validateOtp(String email, String otp) {
        OTP storedOtp = otpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP inválido o expirado."));

        if (!otp.equals(storedOtp.getCode())) {
            throw new RuntimeException("El OTP es inválido.");
        }

        otpRepository.delete(storedOtp);
    }

    // Validar OTP sin eliminarlo (usado en verify-reset-otp)
    public void validateOtpWithoutDeleting(String email, String otp) {
        OTP storedOtp = otpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Código OTP inválido o expirado."));

        if (!otp.equals(storedOtp.getCode())) {
            throw new RuntimeException("Código OTP inválido.");
        }
        // NO eliminamos el OTP aquí, se eliminará en resetPassword
    }

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1000000));
    }
}