package com.uade.ritmofitapi.service;

import io.jsonwebtoken.io.Decoders;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;


@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;
    @Value("${security.jwt.expiration-ms}")
    private long expiration;


    public String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Token inválido por cualquier motivo (expirado, malformado, etc.)
            return false;
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
