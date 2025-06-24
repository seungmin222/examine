package com.example.examine.service.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private Duration accessExpiration;
    private Duration refreshExpiration;

    // getters and setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getAccessExpiration() {
        return accessExpiration;
    }

    public void setAccessExpiration(Duration accessExpiration) {
        this.accessExpiration = accessExpiration;
    }

    public Duration getRefreshExpiration() {
        return refreshExpiration;
    }

    public void setRefreshExpiration(Duration refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

}
