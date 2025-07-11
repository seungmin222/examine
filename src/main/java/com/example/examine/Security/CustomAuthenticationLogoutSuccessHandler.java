package com.example.examine.Security;

import com.example.examine.service.Redis.JwtParser;
import com.example.examine.service.Redis.RedisService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationLogoutSuccessHandler implements LogoutSuccessHandler {

    private final RedisService redisService;
    private final JwtParser jwtParser;

    public CustomAuthenticationLogoutSuccessHandler(RedisService redisService,
                                                    JwtParser jwtParser) {
        this.redisService = redisService;
        this.jwtParser = jwtParser;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        String token = extractAccessTokenFromCookies(request);
        String username = null;

        if (token != null) {
            username = jwtParser.extractUsername(token);
        }

        // Redis 토큰 삭제 (있을 경우만)
        if (username != null) {
            redisService.deleteAllTokens(username);
        }

        // 쿠키는 항상 삭제
        deleteCookie("access", response);
        deleteCookie("refresh", response);

        response.setStatus(HttpServletResponse.SC_OK); // 👈 200 명시
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"로그아웃 성공\"}");

    }

    private String extractAccessTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("access".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void deleteCookie(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 개발 환경에선 false
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
