package com.dms.controller;

import com.dms.dto.AuthDto;
import com.dms.dto.UserDto;
import com.dms.entity.User;
import com.dms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/register")
    public ResponseEntity<UserDto.Response> register(@RequestBody AuthDto.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // New endpoint to verify current authenticated user and token validity
    @GetMapping("/me")
    public ResponseEntity<UserDto.Response> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        UserDto.Response resp = UserDto.Response.builder()
                .id(user.getId())
                .staffId(user.getStaffId())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
        return ResponseEntity.ok(resp);
    }
}
