package com.uade.ritmofitapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de CORS (Cross-Origin Resource Sharing)
 * para la aplicación Spring Boot.
 *
 * Esto le da permiso explícito al frontend (ej. localhost:8081)
 * para que pueda realizar peticiones a este backend (ej. localhost:8080).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplica esta regla a todos los endpoints bajo /api/
            
            // Orígenes permitidos (la clave está aquí):

            .allowedOriginPatterns(
                    "http://localhost:*",              // Cualquier puerto en localhost
                    "https://*.tunnel.expo.dev"        // Cualquier subdominio de Expo
            )

            .allowedOrigins(
                "http://localhost:8081", // Origen de Expo Web en desarrollo
                "https://*.tunnel.expo.dev" // Origen si usa el túnel de Expo
            ) 
            
            // Métodos HTTP que permitimos (GET, POST, etc.)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            
            // Cabeceras que permitimos (ej. Authorization para el token JWT)
            .allowedHeaders("*")
            
            // Permite que el frontend envíe credenciales (cookies, tokens)
            .allowCredentials(true);
    }
}
