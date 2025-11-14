package com.uade.ritmofitapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@Service
public class RateLimitService {

    // Mapa: email -> lista de timestamps de requests
    private final Map<String, List<LocalDateTime>> otpRequests = new ConcurrentHashMap<>();

    /**
     * Valida si un email puede solicitar un nuevo OTP.
     * Reglas:
     * - Máximo 1 OTP cada 30 segundos
     * - Máximo 5 OTP por hora
     *
     * @param email Email a validar
     * @throws RuntimeException si se exceden los límites
     */
    public void checkOtpRateLimit(String email) {
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> requests = otpRequests.computeIfAbsent(email, k -> new ArrayList<>());

        // Limpiar requests antiguos (más de 1 hora)
        requests.removeIf(timestamp -> timestamp.isBefore(now.minusHours(1)));

        // Verificar: último request fue hace menos de 2 minutos
        if (!requests.isEmpty()) {
            LocalDateTime lastRequest = requests.get(requests.size() - 1);
            if (lastRequest.isAfter(now.minusSeconds(30))) {
                long secondsToWait = java.time.Duration.between(now, lastRequest.plusSeconds(30)).getSeconds();
                throw new RuntimeException(
                    "Debes esperar " + secondsToWait + " segundos antes de solicitar un nuevo código OTP."
                );
            }
        }

        // Verificar: menos de 5 requests en la última hora
        if (requests.size() >= 5) {
            throw new RuntimeException(
                "Has alcanzado el límite de 5 códigos OTP por hora. Intenta más tarde."
            );
        }

        // Agregar el timestamp actual
        requests.add(now);
        log.info("Rate limit check passed for email: {}. Requests in last hour: {}", email, requests.size());
    }

    /**
     * Limpia los rate limits de un email (útil para testing o después de verificación exitosa)
     */
    public void clearRateLimit(String email) {
        otpRequests.remove(email);
        log.info("Rate limit cleared for email: {}", email);
    }
}
