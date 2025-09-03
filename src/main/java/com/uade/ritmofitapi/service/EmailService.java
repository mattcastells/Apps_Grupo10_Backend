package com.uade.ritmofitapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("no-reply@ritmofit.com.ar");

            mailSender.send(message);
            log.info("Email OTP enviado exitosamente a {}", to);
        } catch (Exception e) {
            log.error("Error al enviar el email: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
