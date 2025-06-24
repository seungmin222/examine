package com.example.examine.service.EntityService;

import com.example.examine.dto.request.UserRequest;
import com.example.examine.dto.response.UserResponse;
import com.example.examine.entity.User;
import com.example.examine.repository.UserRepository;
import com.example.examine.service.redis.JwtProperties;
import com.example.examine.service.redis.RedisService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final JwtProperties jwtProperties;

    public UserService(UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       RedisService redisService,
                       JwtProperties jwtProperties) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
        this.jwtProperties = jwtProperties;
    }

    public boolean findByUsername(String username) {
        return userRepo.findByUsername(username).isPresent();
    }


    public void create(UserRequest req) {
        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        userRepo.save(user);
    }

    public UserResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthenticated");
        }

        String username = authentication.getName();

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        // ✅ 사용자 활동 감지 → Refresh TTL 연장
        redisService.extendTTL("refresh", username, jwtProperties.getRefreshExpiration());

        return UserResponse.fromEntity(user);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));
        return user;
    }
}
