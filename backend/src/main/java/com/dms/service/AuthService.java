package com.dms.service;

import com.dms.dto.AuthDto;
import com.dms.dto.UserDto;
import com.dms.entity.User;
import com.dms.repository.UserRepository;
import com.dms.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    
    @Transactional
    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getStaffId(), request.getPassword())
        );
        
        User user = (User) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(user);
        
        return AuthDto.LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .user(UserDto.Response.builder()
                        .id(user.getId())
                        .staffId(user.getStaffId())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .build())
                .build();
    }
    
    @Transactional
    public UserDto.Response register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByStaffId(request.getStaffId())) {
            throw new RuntimeException("工号已存在: " + request.getStaffId());
        }
        
        User user = User.builder()
                .staffId(request.getStaffId())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .status("ACTIVE")
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        
        user = userRepository.save(user);
        
        return UserDto.Response.builder()
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
    }
}
