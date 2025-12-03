package com.dms.dto;

import com.dms.entity.LoanRecord.ApprovalStatus;
import com.dms.entity.LoanRecord.ReturnStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoanRecordDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyRequest {
        @NotNull(message = "设备ID不能为空")
        private Long deviceId;
        
        @NotNull(message = "预计归还日期不能为空")
        @Future(message = "预计归还日期必须是将来的日期")
        private LocalDate expectedReturnDate;
        
        @Size(max = 500, message = "借用目的不能超过500个字符")
        private String purpose;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApproveRequest {
        @Size(max = 500, message = "驳回原因不能超过500个字符")
        private String rejectionReason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long deviceId;
        private String deviceName;
        private Long applicantId;
        private String applicantName;
        private String applicantStaffId;
        private LocalDateTime applyDate;
        private LocalDate expectedReturnDate;
        private LocalDateTime actualReturnDate;
        private ApprovalStatus approvalStatus;
        private ReturnStatus returnStatus;
        private String purpose;
        private String approvedByName;
        private LocalDateTime approvedAt;
        private String rejectionReason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
