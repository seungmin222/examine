package com.example.examine.controller;

import com.example.examine.service.redis.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/redis")
public class RedisController {

    private final RedisService redisService;

    public RedisController(RedisService redisService) {
        this.redisService = redisService;
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> saveValue(
            @RequestParam String prefix,
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(required = false) Duration ttl // 기본 TTL 없음
    ) {
        String fullKey = prefix + ":" + key;
        redisService.saveToken(prefix, key, value, ttl != null ? ttl : Duration.ofHours(1));
        return ResponseEntity.ok(Map.of("result", "저장됨", "key", fullKey));
    }

    @GetMapping("/get")
    public ResponseEntity<Map<String, String>> getValue(
            @RequestParam String prefix,
            @RequestParam String key
    ) {
        String fullKey = prefix + ":" + key;
        String value = redisService.getToken(prefix, key);
        if (value == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "값 없음", "key", fullKey));
        }
        return ResponseEntity.ok(Map.of("result", value, "key", fullKey));
    }

    @PostMapping("/api/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        return redisService.refresh(request, response);
    }
}
