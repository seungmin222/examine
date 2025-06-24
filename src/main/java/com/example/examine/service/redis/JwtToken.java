package com.example.examine.service.redis;

import com.example.examine.entity.User;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtToken {

    private final JwtProperties jwtProperties;

    public JwtToken(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getAccessExpiration());
        Date issuedAt = Date.from(now);
        Date expiresAt = Date.from(expiry);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .claim("level", user.getLevel())
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret())
                .compact();
    }

    public String createRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getRefreshExpiration());
        Date issuedAt = Date.from(now);
        Date expiresAt = Date.from(expiry);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .claim("username", user.getUsername())
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret())
                .compact();
    }
}
