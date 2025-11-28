package com.uade.ritmofitapi.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF, común en APIs REST
                .authorizeHttpRequests(auth -> auth
                        // Endpoints de autenticación (login, OTP) siempre deben ser públicos
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Permitimos el acceso público al calendario de clases
                        .requestMatchers("/api/v1/schedule/**").permitAll()

                        // Permitimos el acceso público a las ubicaciones/sedes
                        .requestMatchers("/api/v1/locations/**").permitAll()

                        // Permitir acceso a endpoints de mock para desarrollo
                        .requestMatchers("/api/v1/mock/**").permitAll()

                        // Permitir acceso a /error (Spring Boot error page)
                        .requestMatchers("/error").permitAll()

                        // Cualquier otra petición que no coincida con las reglas anteriores, requerirá autenticación.
                        .anyRequest().authenticated()
                )
                // Le indicamos a Spring que no cree sesiones, ya que usaremos un enfoque "stateless" con tokens.
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        // Token faltante o inválido: devolver 401
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"timestamp\":\"" + java.time.Instant.now() + "\"," +
                                            "\"status\":401," +
                                            "\"error\":\"Unauthorized\"," +
                                            "\"message\":\"Autenticación requerida. Por favor, inicia sesión.\"}"
                            );
                        })
                        // Autenticado pero sin permisos: devolver 403
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"timestamp\":\"" + java.time.Instant.now() + "\"," +
                                            "\"status\":403," +
                                            "\"error\":\"Forbidden\"," +
                                            "\"message\":\"No tenés permisos para acceder a este recurso.\"}"
                            );
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt es el estandar de facto para el hashing de contraseñas.
        // Incluye un "salt" aleatorio para proteger contra ataques de rainbow table.
        return new BCryptPasswordEncoder();
    }
}
