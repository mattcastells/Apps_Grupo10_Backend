package com.uade.ritmofitapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio para envío de emails usando Gmail SMTP.
 *
 * Configuración requerida en .env:
 * - GMAIL_USERNAME: Tu email de Gmail (ej: tucorreo@gmail.com)
 * - GMAIL_APP_PASSWORD: Contraseña de aplicación generada en Google
 *
 * Cómo obtener la contraseña de aplicación:
 * 1. Ve a https://myaccount.google.com/security
 * 2. Activa la verificación en 2 pasos (si no la tienes)
 * 3. Busca "Contraseñas de aplicaciones" (App passwords)
 * 4. Genera una nueva contraseña para "RitmoFit Backend"
 * 5. Copia la contraseña de 16 caracteres
 *
 * Documentación: https://support.google.com/accounts/answer/185833
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Envía un email usando Gmail SMTP.
     *
     * @param to Email del destinatario
     * @param subject Asunto del email
     * @param body Contenido del email (texto plano)
     * @throws RuntimeException Si falla el envío del email
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            log.info("Preparando envío de email a {} con asunto: {}", to, subject);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            // Enviar email
            log.debug("Enviando email a través de Gmail SMTP...");
            mailSender.send(message);

            log.info("✅ Email enviado exitosamente a {} a través de Gmail SMTP", to);

        } catch (Exception e) {
            log.error("❌ Error al enviar el email a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("No se pudo enviar el email: " + e.getMessage(), e);
        }
    }

}
