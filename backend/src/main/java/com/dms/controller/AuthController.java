package com.dms.controller;

import com.dms.dto.AuthDto;
import com.dms.dto.UserDto;
import com.dms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
