package com.example.examine.Security;

import com.example.examine.entity.User;
import com.example.examine.service.redis.JwtProperties;
import com.example.examine.service.redis.JwtToken;
import com.example.examine.service.redis.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtToken jwtToken;
    private final JwtProperties jwtProperties;
    private final RedisService redisService;

    public CustomAuthenticationSuccessHandler(JwtToken jwtToken,
                                              JwtProperties jwtProperties,
                                              RedisService redisService) {
        this.jwtToken = jwtToken;
        this.jwtProperties = jwtProperties;
        this.redisService = redisService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 로그인한 사용자 정보 가져오기
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // JWT 생성
        String token = jwtToken.createAccessToken((User) userDetails); // 필요하면 UserDetails → User 변환

        // Redis에 토큰 저장
        redisService.saveToken("access",username, token, jwtProperties.getAccessExpiration());

        // JWT를 쿠키에 담기
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);         // JavaScript에서 접근 못하게
        cookie.setPath("/");              // 전체 경로에서 쿠키 사용 가능
        cookie.setMaxAge((int) jwtProperties.getAccessExpiration().getSeconds());    // 2시간 (초 단위)

        cookie.setSecure(true); // HTTPS 환경에서만 전송하고 싶으면 true
        response.addCookie(cookie); // 응답에 쿠키 추가

        String refreshToken = jwtToken.createRefreshToken((User) userDetails);
        redisService.saveToken("refresh", username, refreshToken, jwtProperties.getRefreshExpiration());

        Cookie refreshCookie = new Cookie("refresh", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) jwtProperties.getRefreshExpiration().getSeconds());
        response.addCookie(refreshCookie);


        // 리다이렉트 처리
        String redirect = request.getParameter("redirect");
        if (redirect != null && !redirect.isBlank()) {
            response.sendRedirect(redirect);
        } else {
            response.sendRedirect("/");
        }
    }
}

