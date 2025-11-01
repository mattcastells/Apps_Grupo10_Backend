// Paquete de su aplicación (ajuste si es necesario)
package com.uade.ritmofitapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de CORS para la aplicación.
 * Esto permite que el frontend (ej. localhost:8081)
 * pueda consumir la API del backend (ej. localhost:8080).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplica la política a todos los endpoints bajo /api/

                // Aquí está la clave:
                // Le decimos al backend que permita peticiones DESDE este origen.
                .allowedOrigins("http://localhost:8081")

                // Si usa el "tunnel" de Expo, la URL es pública.
                // Puede agregar la URL del túnel aquí, o usar un comodín para desarrollo.
                // Ejemplo con comodín para túnel de Expo:
                // .allowedOrigins("http://localhost:8081", "https://*.tunnel.expo.dev")

                // Métodos HTTP permitidos
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                // Cabeceras permitidas (importante para enviar el token JWT)
                .allowedHeaders("*")

                // Permite el envío de credenciales (como cookies o tokens de autorización)
                .allowCredentials(true);
    }
}