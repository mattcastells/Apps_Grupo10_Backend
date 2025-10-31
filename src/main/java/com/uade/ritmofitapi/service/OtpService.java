// Archivo: com.uade.ritmofitapi.service.OtpService.java
package com.uade.ritmofitapi.service;

import com.uade.ritmofitapi.model.OTP;
import com.uade.ritmofitapi.repository.OtpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Slf4j
@Service
public class OtpService {

    private final OtpRepository otpRepository;
    // private final EmailService emailService; // Comentado para desarrollo

    public OtpService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
        // this.emailService = emailService; // Comentado para desarrollo
    }

    public void sendOtpForVerification(String email) {
        String otp = generateOtp();
        OTP userOtp = new OTP(email, otp);
        otpRepository.save(userOtp);

        // MODO DESARROLLO: Mostrar OTP en consola en lugar de enviar email
        log.info("========================================");
        log.info("🔐 OTP GENERADO PARA: {}", email);
        log.info("📧 CÓDIGO: {}", otp);
        log.info("⏰ Válido por 10 minutos");
        log.info("========================================");
        
        // Comentado para desarrollo - descomentar cuando tengas email configurado
        // String subject = "Verifica tu cuenta en RitmoFit";
        // String body = "Hola,\n\nUsa este código para verificar tu email: " + otp + "\n\nEl código es válido por 10 minutos.";
        // emailService.sendEmail(email, subject, body);
    }

    public void validateOtp(String email, String otp) {
        OTP storedOtp = otpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP inválido o expirado."));

        if (!otp.equals(storedOtp.getCode())) {
            throw new RuntimeException("El OTP es inválido.");
        }

        otpRepository.delete(storedOtp);
    }

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1000000));
    }
}