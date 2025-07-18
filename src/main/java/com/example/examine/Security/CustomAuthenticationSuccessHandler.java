package com.example.examine.Security;

import com.example.examine.entity.User.User;
import com.example.examine.repository.UserRepository.UserRepository;
import com.example.examine.service.Redis.JwtProperties;
import com.example.examine.service.Redis.JwtToken;
import com.example.examine.service.Redis.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtToken jwtToken;
    private final JwtProperties jwtProperties;
    private final RedisService redisService;
    private final UserRepository userRepository; // ✅ UserService 대신 이것만 주입

    public CustomAuthenticationSuccessHandler(
            JwtToken jwtToken,
            JwtProperties jwtProperties,
            RedisService redisService,
            UserRepository userRepository
    ) {
        this.jwtToken = jwtToken;
        this.jwtProperties = jwtProperties;
        this.redisService = redisService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // ✅ 직접 User 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        String accessToken = jwtToken.createAccessToken(user);
        String refreshToken = jwtToken.createRefreshToken(user);

        redisService.saveToken("access", username, accessToken, jwtProperties.getAccessExpiration());
        redisService.saveToken("refresh", username, refreshToken, jwtProperties.getRefreshExpiration());

        // Access 토큰
        Cookie access = createCookie("access", accessToken, (int) jwtProperties.getAccessExpiration().getSeconds());
        response.addCookie(access);

// Refresh 토큰
        Cookie refresh = createCookie("refresh", refreshToken, (int) jwtProperties.getRefreshExpiration().getSeconds());
        response.addCookie(refresh);


        String redirect = request.getParameter("redirect");
        if (redirect != null && !redirect.isBlank()) {
            response.sendRedirect(redirect);
        } else {
            response.sendRedirect("/");
        }
    }
    private Cookie createCookie(String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);       // XSS 방지
        cookie.setSecure(false);        // 개발 환경일 경우 false
        cookie.setPath("/");            // 전체 경로에서 유효
        cookie.setMaxAge(maxAgeSeconds); // 유효 시간 (초 단위)
        return cookie;
    }

}



