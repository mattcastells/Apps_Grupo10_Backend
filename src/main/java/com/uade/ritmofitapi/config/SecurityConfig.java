package com.uade.ritmofitapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF, común en APIs REST
                .authorizeHttpRequests(auth -> auth
                        // Endpoints de autenticación (login, OTP) siempre deben ser públicos
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Permitimos el acceso público al calendario de clases
                        .requestMatchers("/api/v1/schedule/**").permitAll()

                        // Por ahora, para poder probar el endpoint de booking, también lo dejaremos abierto.
                        // ¡¡IMPORTANTE!! Esto es temporal. Lo aseguraremos cuando implementemos JWT.
                        .requestMatchers("/api/v1/booking/**").permitAll()

                        // Cualquier otra petición que no coincida con las reglas anteriores, requerirá autenticación.
                        .anyRequest().authenticated()
                )
                // Le indicamos a Spring que no cree sesiones, ya que usaremos un enfoque "stateless" con tokens.
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt es el estandar de facto para el hashing de contraseñas.
        // Incluye un "salt" aleatorio para proteger contra ataques de rainbow table.
        return new BCryptPasswordEncoder();
    }
}