package com.example.examine.service.Redis;

import com.example.examine.controller.DetailController;
import com.example.examine.entity.Page;
import com.example.examine.entity.User;
import com.example.examine.repository.PageRepository;
import com.example.examine.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class RedisService {
    private static final Logger log = LoggerFactory.getLogger(DetailController.class);
    private final UserRepository userRepo;
    private final PageRepository pageRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtParser jwtParser;
    private final JwtToken jwtToken;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redisViewTemplate;

    public void saveToken(String prefix, String username, String token, Duration duration) {
        redisTemplate.opsForValue().set(prefix + ":" + username, token, duration);
    }

    public void extendTTL(String prefix, String username, Duration ttl) {
        String key = prefix + ":" + username;
        redisTemplate.expire(key, ttl);
    }

    public String getToken(String prefix, String username) {

        return (String) redisTemplate.opsForValue().get(prefix + ":" + username);
    }

    public void deleteToken(String prefix, String username) {
        redisTemplate.delete(prefix + ":" + username);
    }

    public void deleteAllTokens(String username) {
        deleteToken("token", username);
        deleteToken("refresh", username);
    }


    public ResponseEntity<String> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null || jwtParser.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("RefreshToken 만료");
        }

        String username = jwtParser.extractUsername(refreshToken);
        String stored = getToken("refresh", username);

        if (stored == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("세션 만료: Refresh Token 없음");
        }

        if (!refreshToken.equals(stored)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("RefreshToken 불일치");
        }

        User user = userRepo.findByUsername(username).orElseThrow();
        String newAccessToken = jwtToken.createAccessToken(user);
        saveToken("token", username, newAccessToken, jwtProperties.getAccessExpiration());

        Cookie access = new Cookie("jwt", newAccessToken);
        access.setHttpOnly(true);
        access.setPath("/");
        access.setMaxAge((int) jwtProperties.getAccessExpiration().getSeconds());
        response.addCookie(access);

        // 🔁 Refresh Token 슬라이딩 갱신 (1일 이하 남았으면)
        Duration remaining = jwtParser.getRemainingTime(refreshToken);
        if (remaining.compareTo(Duration.ofDays(1)) < 0) {
            String newRefreshToken = jwtToken.createRefreshToken(user);
            saveToken("refresh" , username, newRefreshToken, jwtProperties.getRefreshExpiration());

            Cookie newRefresh = new Cookie("refresh", newRefreshToken);
            newRefresh.setHttpOnly(true);
            newRefresh.setPath("/");
            newRefresh.setMaxAge((int) jwtProperties.getRefreshExpiration().getSeconds());
            response.addCookie(newRefresh);
        }

        return ResponseEntity.ok("AccessToken 재발급 완료");
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void incrementPageView(Long pageId) {
        String key = "page:views:" + pageId;

        // ✅ Redis에 키 없으면 DB에서 가져와 초기화
        if (!Boolean.TRUE.equals(redisViewTemplate.hasKey(key))) {
            Page page = pageRepo.findById(pageId)
                    .orElseThrow(() -> new RuntimeException("Page not found"));
            redisViewTemplate.opsForValue().set(key, String.valueOf(page.getViewCount()));
        }

        // ✅ 키가 있든 없든 증가시킴
            redisViewTemplate.opsForValue().increment(key);
    }


    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void syncViewsToDatabase() {
        Set<String> keys = redisViewTemplate.keys("page:views:*");
        if (keys == null) return;

        for (String key : keys) {
            try {
                String pageIdStr = key.substring("page:views:".length());
                Long pageId = Long.valueOf(pageIdStr);

                String countStr = redisViewTemplate.opsForValue().get(key);
                if (countStr == null) continue;

                Long count = Long.valueOf(countStr);

                pageRepo.findById(pageId).ifPresent(page -> {
                    page.setViewCount(count);
                    pageRepo.save(page);
                    redisViewTemplate.delete(key); // ✅ 삭제 추가
                });
            } catch (Exception e) {
                log.warn("조회수 동기화 실패: key={}, error={}", key, e.getMessage());
            }
        }
    }

}
