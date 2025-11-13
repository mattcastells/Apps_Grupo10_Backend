package com.uade.ritmofitapi.config;

import com.uade.ritmofitapi.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        // No aplicar JWT filter en endpoints públicos
        return path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/schedule/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si no hay header Authorization o no empieza con "Bearer ", continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraer el token (quitando "Bearer ")
            final String jwt = authHeader.substring(7);

            // Validar el token
            if (jwtService.validateToken(jwt)) {
                // Extraer el userId del token
                String userId = jwtService.getUserIdFromToken(jwt);

                // Crear la autenticación con el userId como principal
                // El userId será accesible mediante SecurityContextHolder.getContext().getAuthentication().getName()
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId,  // principal (será retornado por auth.getName())
                        null,    // credentials (no necesarias después de autenticar)
                        Collections.emptyList()  // authorities (roles/permisos - vacío por ahora)
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Setear la autenticación en el contexto de Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Si hay algún error al procesar el token, simplemente no autenticamos
            // (el endpoint puede ser público o retornar 403 si es protegido)
        }

        filterChain.doFilter(request, response);
    }
}
