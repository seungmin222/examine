package com.example.examine.controller;

import com.example.examine.dto.request.UserRequest;
import com.example.examine.dto.response.UserResponse;
import com.example.examine.entity.User;
import com.example.examine.service.EntityService.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;

    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam UserRequest req) {
        if (userService.findByUsername(req.username())) {
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다");
        }


        userService.create(req);

        return ResponseEntity.ok("가입 성공");
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {
        return userService.getCurrentUser(authentication);
    }
}
