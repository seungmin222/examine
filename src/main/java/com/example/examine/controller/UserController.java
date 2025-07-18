package com.example.examine.controller;

import com.example.examine.dto.request.UserRequest;
import com.example.examine.dto.response.UserResponse;
import com.example.examine.service.EntityService.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> create(@Valid @RequestBody UserRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("에러 발생");
        }
        return userService.create(request);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/pw")
    public ResponseEntity<String> updatePassword(@RequestParam String password) {
        return userService.updatePassword(password);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/role")
    public ResponseEntity<String> updateRole(@RequestParam String role) {
        return userService.updateRole(role);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {
        return userService.getCurrentUser(authentication);
    }

    @GetMapping("/duplication")
    public boolean checkDuplication(@RequestParam String username) {
        return userService.checkDuplication(username);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMe(@RequestParam UserRequest req) {
        return userService.deleteMe(req);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("bookmark/{id}")
    public ResponseEntity<String> addBookmark(Authentication authentication, @PathVariable Long id) {
        userService.addBookmark(authentication, id);
        return ResponseEntity.ok("북마크 추가 완료");
    }

    @PostMapping("/cart/{productId}")
    public ResponseEntity<String> addCart(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication authentication
    ) {
        return userService.addCart(authentication, productId, quantity);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/bookmark/{id}")
    public ResponseEntity<String> deleteBookmark(Authentication authentication, @PathVariable Long id) {
        userService.deleteBookmark(authentication, id);
        return ResponseEntity.ok("북마크 삭제 완료");
    }

    @DeleteMapping("/cart/{productId}")
    public ResponseEntity<String> removeCart(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        return userService.removeCart(authentication, productId);
    }
}
