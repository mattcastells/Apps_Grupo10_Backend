package com.uade.ritmofitapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para envío de emails usando Elastic Email API.
 *
 * Configuración requerida en .env:
 * - ELASTIC_EMAIL_API_KEY: Tu API Key de Elastic Email
 * - ELASTIC_EMAIL_FROM: Email del remitente (debe estar verificado)
 * - ELASTIC_EMAIL_FROM_NAME: Nombre del remitente
 *
 * Documentación API: https://elasticemail.com/developers/api-documentation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    // API v4 endpoint (nuevo)
    private static final String ELASTIC_EMAIL_API_URL = "https://api.elasticemail.com/v4/emails";

    @Value("${elastic.email.api-key}")
    private String apiKey;

    @Value("${elastic.email.from}")
    private String fromEmail;

    @Value("${elastic.email.from-name}")
    private String fromName;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Envía un email usando Elastic Email API.
     *
     * @param to Email del destinatario
     * @param subject Asunto del email
     * @param body Contenido del email (texto plano)
     * @throws RuntimeException Si falla el envío del email
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            log.info("Preparando envío de email a {} con asunto: {}", to, subject);

            // Validar que tenemos la API key configurada
            if (apiKey == null || apiKey.isEmpty() || apiKey.equals("tu_api_key_de_elastic_email_aqui")) {
                log.error("ELASTIC_EMAIL_API_KEY no está configurada correctamente");
                throw new RuntimeException("Configuración de email no válida. Por favor configura ELASTIC_EMAIL_API_KEY en .env");
            }

            // Construir request body para API v4 (JSON)
            Map<String, Object> emailRequest = new HashMap<>();

            // Recipients
            Map<String, String> recipient = new HashMap<>();
            recipient.put("Email", to);
            emailRequest.put("Recipients", new Map[]{recipient});

            // Content
            Map<String, Object> content = new HashMap<>();
            content.put("Body", new Map[]{
                Map.of("ContentType", "PlainText", "Content", body)
            });
            content.put("From", fromEmail);
            content.put("Subject", subject);
            emailRequest.put("Content", content);

            // Configurar headers para API v4
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-ElasticEmail-ApiKey", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailRequest, headers);

            // Enviar request POST
            log.debug("Enviando email a Elastic Email API v4: {}", ELASTIC_EMAIL_API_URL);
            ResponseEntity<String> response = restTemplate.postForEntity(ELASTIC_EMAIL_API_URL, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                log.info("✅ Email enviado exitosamente a {} a través de Elastic Email v4", to);
                log.debug("Response: {}", response.getBody());
            } else {
                log.error("Error al enviar email. Status code: {}, Response: {}",
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Error al enviar email: " + response.getBody());
            }

        } catch (Exception e) {
            log.error("❌ Error al enviar el email a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("No se pudo enviar el email: " + e.getMessage(), e);
        }
    }

}
