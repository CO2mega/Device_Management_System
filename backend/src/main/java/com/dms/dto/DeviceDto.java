package com.dms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DeviceDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "设备名称不能为空")
        @Size(max = 100, message = "设备名称不能超过100个字符")
        private String name;
        
        @Size(max = 50, message = "设备类型不能超过50个字符")
        private String type;
        
        @Size(max = 200, message = "存放位置不能超过200个字符")
        private String location;
        
        private LocalDate purchaseDate;
        
        private String status;
        
        private Boolean isLoaned;
        
        private LocalDate loanDate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String type;
        private String location;
        private LocalDate purchaseDate;
        private Boolean isLoaned;
        private String status;
        private LocalDate loanDate;
        private String currentHolderName;
        private Long currentHolderId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
