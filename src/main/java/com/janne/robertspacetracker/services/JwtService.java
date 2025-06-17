package com.janne.robertspacetracker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class JwtService {

    private final ObjectMapper objectMapper;
    @Getter
    private final long jwtValidityDuration;
    private final Key signingKey;
    private final JwtParser jwtParser;

    public JwtService(@Value("${app.jwt.duration}") long jwtValidityDuration, @Value("${app.jwt.secret}") String jwtSecret, ObjectMapper objectMapper) {
        this.jwtValidityDuration = jwtValidityDuration;
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.objectMapper = objectMapper;
        this.jwtParser = Jwts.parserBuilder().setSigningKey(this.signingKey).build();
    }

    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
            .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
            .setIssuedAt(new Date())
            .setAudience("Robert Space Tracker")
            .setSubject((String) claims.get("email"))
            .setExpiration(new Date(new Date().getTime() + jwtValidityDuration))
            .addClaims(claims)
            .signWith(signingKey)
            .compact();
    }

    public String getJwtOwner(String token) {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();
        if (claims.getExpiration().before(new Date())) {
            return null;
        }
        if (!"Robert Space Tracker".equals(claims.getAudience())) {
            return null;
        }
        return claims.getSubject();
    }
}
