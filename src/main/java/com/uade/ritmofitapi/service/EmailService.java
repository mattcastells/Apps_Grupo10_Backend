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

    private static final String ELASTIC_EMAIL_API_URL = "https://api.elasticemail.com/v2/email/send";

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

            // Construir URL con parámetros
            String url = buildElasticEmailUrl(to, subject, body);

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> request = new HttpEntity<>(headers);

            // Enviar request POST
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Email enviado exitosamente a {} a través de Elastic Email", to);
            } else {
                log.error("Error al enviar email. Status code: {}, Response: {}",
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Error al enviar email: " + response.getBody());
            }

        } catch (Exception e) {
            log.error("Error al enviar el email a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("No se pudo enviar el email: " + e.getMessage(), e);
        }
    }

    /**
     * Construye la URL con todos los parámetros para Elastic Email API v2.
     * Usamos URL encoding para manejar caracteres especiales correctamente.
     */
    private String buildElasticEmailUrl(String to, String subject, String body) {
        Map<String, String> params = new HashMap<>();
        params.put("apikey", apiKey);
        params.put("from", fromEmail);
        params.put("fromName", fromName);
        params.put("to", to);
        params.put("subject", subject);
        params.put("bodyText", body);
        params.put("isTransactional", "true");

        StringBuilder urlBuilder = new StringBuilder(ELASTIC_EMAIL_API_URL);
        urlBuilder.append("?");

        params.forEach((key, value) -> {
            urlBuilder.append(key)
                     .append("=")
                     .append(urlEncode(value))
                     .append("&");
        });

        // Remover el último &
        String url = urlBuilder.toString();
        return url.substring(0, url.length() - 1);
    }

    /**
     * URL encode simple para los parámetros.
     */
    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            log.error("Error al encodear URL: {}", e.getMessage());
            return value;
        }
    }

    /**
     * Envía un email HTML usando Elastic Email API.
     *
     * @param to Email del destinatario
     * @param subject Asunto del email
     * @param htmlBody Contenido del email en formato HTML
     * @throws RuntimeException Si falla el envío del email
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            log.info("Preparando envío de email HTML a {} con asunto: {}", to, subject);

            if (apiKey == null || apiKey.isEmpty() || apiKey.equals("tu_api_key_de_elastic_email_aqui")) {
                log.error("ELASTIC_EMAIL_API_KEY no está configurada correctamente");
                throw new RuntimeException("Configuración de email no válida");
            }

            Map<String, String> params = new HashMap<>();
            params.put("apikey", apiKey);
            params.put("from", fromEmail);
            params.put("fromName", fromName);
            params.put("to", to);
            params.put("subject", subject);
            params.put("bodyHtml", htmlBody);
            params.put("isTransactional", "true");

            StringBuilder urlBuilder = new StringBuilder(ELASTIC_EMAIL_API_URL);
            urlBuilder.append("?");

            params.forEach((key, value) -> {
                urlBuilder.append(key)
                         .append("=")
                         .append(urlEncode(value))
                         .append("&");
            });

            String url = urlBuilder.toString();
            url = url.substring(0, url.length() - 1);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Email HTML enviado exitosamente a {} a través de Elastic Email", to);
            } else {
                log.error("Error al enviar email HTML. Status code: {}, Response: {}",
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Error al enviar email HTML: " + response.getBody());
            }

        } catch (Exception e) {
            log.error("Error al enviar el email HTML a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("No se pudo enviar el email HTML: " + e.getMessage(), e);
        }
    }
}
