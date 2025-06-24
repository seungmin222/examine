package com.example.examine.Security;

import com.example.examine.service.redis.JwtParser;
import com.example.examine.service.redis.RedisService;
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
        this.jwtParser = jwtParser;
        this.redisService = redisService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // "Bearer " 제거
            String username = jwtParser.extractUsername(token);

            if (username != null) {
                redisService.deleteAllTokens(username); // Access + Refresh 삭제

                // Access Token 쿠키 삭제
                Cookie access = new Cookie("jwt", null);
                access.setHttpOnly(true);
                access.setPath("/");
                access.setMaxAge(0);
                response.addCookie(access);

                // Refresh Token 쿠키 삭제
                Cookie refresh = new Cookie("refresh", null);
                refresh.setHttpOnly(true);
                refresh.setPath("/");
                refresh.setMaxAge(0);
                response.addCookie(refresh);
            }
        }

        String redirect = request.getParameter("redirect");
        if (redirect != null && !redirect.isBlank()) {
            response.sendRedirect(redirect);
        } else {
            response.sendRedirect("/");
        }
    }

}
