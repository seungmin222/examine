package com.example.examine.service.Redis;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtParser {

    private final Key secretKey;

    public JwtParser(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // username 추출
    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    // role 추출
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // level 추출
    public Integer extractLevel(String token) {
        return extractAllClaims(token).get("level", Integer.class);
    }

    // 만료 여부 확인 (true면 만료됨)
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new java.util.Date());
    }

    public Duration getRemainingTime(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return Duration.between(LocalDateTime.now(), expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    // 내부적으로 Claim 전체 추출
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
