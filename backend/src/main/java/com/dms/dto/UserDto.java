package com.dms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "工号不能为空")
        @Size(max = 50, message = "工号不能超过50个字符")
        private String staffId;
        
        @NotBlank(message = "用户名不能为空")
        @Size(max = 100, message = "用户名不能超过100个字符")
        private String username;
        
        @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
        private String password;
        
        private String role;
        
        private String status;
        
        @Size(max = 100, message = "邮箱不能超过100个字符")
        private String email;
        
        @Size(max = 20, message = "手机号不能超过20个字符")
        private String phone;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String staffId;
        private String username;
        private String role;
        private String status;
        private String email;
        private String phone;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
