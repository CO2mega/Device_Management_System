package com.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String staffId;
        private String password;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private String tokenType;
        private Long expiresIn;
        private UserDto.Response user;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        private String staffId;
        private String username;
        private String password;
        private String email;
        private String phone;
    }
}
