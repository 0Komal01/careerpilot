package com.careerpilot.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${careerpilot.jwt.secret}") String secret,
                      @Value("${careerpilot.jwt.expiration-minutes}") long minutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = minutes * 60_000;
    }

    public String generate(String email, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder().subject(email).claim("role", role)
                .issuedAt(new Date(now)).expiration(new Date(now + expirationMs))
                .signWith(key).compact();
    }

    /** Throws JwtException if the token is expired, tampered with or malformed. */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
