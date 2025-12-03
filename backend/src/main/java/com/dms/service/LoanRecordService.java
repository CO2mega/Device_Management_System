package com.dms.service;

import com.dms.dto.LoanRecordDto;
import com.dms.entity.Device;
import com.dms.entity.LoanRecord;
import com.dms.entity.LoanRecord.ApprovalStatus;
import com.dms.entity.LoanRecord.ReturnStatus;
import com.dms.entity.User;
import com.dms.repository.DeviceRepository;
import com.dms.repository.LoanRecordRepository;
import com.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoanRecordService {
    
    private final LoanRecordRepository loanRecordRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public Page<LoanRecordDto.Response> findAll(String search, ApprovalStatus status, Pageable pageable) {
        return loanRecordRepository.findByFilters(search, status, pageable)
                .map(this::toResponse);
    }
    
    @Transactional(readOnly = true)
    public LoanRecordDto.Response findById(Long id) {
        LoanRecord record = loanRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借用记录不存在: " + id));
        return toResponse(record);
    }
    
    @Transactional
    public LoanRecordDto.Response apply(Long applicantId, LoanRecordDto.ApplyRequest request) {
        // Get applicant
        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + applicantId));
        
        // Get device with pessimistic lock to prevent concurrent loan applications
        Device device = deviceRepository.findByIdWithLock(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("设备不存在: " + request.getDeviceId()));
        
        // Check if device is already loaned
        if (Boolean.TRUE.equals(device.getIsLoaned())) {
            throw new RuntimeException("设备已被借出，无法申请");
        }
        
        // Check if there's already a pending or approved loan for this device
        boolean hasActiveApplication = loanRecordRepository.existsByDeviceIdAndApprovalStatusAndActualReturnDateIsNull(
                device.getId(), ApprovalStatus.APPROVED);
        if (hasActiveApplication) {
            throw new RuntimeException("设备已有正在进行中的借用，无法申请");
        }
        
        LoanRecord record = LoanRecord.builder()
                .device(device)
                .applicant(applicant)
                .applyDate(LocalDateTime.now())
                .expectedReturnDate(request.getExpectedReturnDate())
                .purpose(request.getPurpose())
                .approvalStatus(ApprovalStatus.PENDING)
                .returnStatus(ReturnStatus.NOT_RETURNED)
                .build();
        
        record = loanRecordRepository.save(record);
        return toResponse(record);
    }
    
    @Transactional
    public LoanRecordDto.Response approve(Long id, Long approverId) {
        LoanRecord record = loanRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借用记录不存在: " + id));
        
        if (record.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("只有待处理的申请可以批准");
        }
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("审批人不存在: " + approverId));
        
        // Get device with pessimistic lock
        Device device = deviceRepository.findByIdWithLock(record.getDevice().getId())
                .orElseThrow(() -> new RuntimeException("设备不存在"));
        
        // Double check device is not already loaned (idempotency check)
        if (Boolean.TRUE.equals(device.getIsLoaned())) {
            throw new RuntimeException("设备已被借出，无法批准此申请");
        }
        
        // Update device status
        device.setIsLoaned(true);
        device.setCurrentHolder(record.getApplicant());
        device.setLoanDate(LocalDate.now());
        deviceRepository.save(device);
        
        // Update loan record
        record.setApprovalStatus(ApprovalStatus.APPROVED);
        record.setApprovedBy(approver);
        record.setApprovedAt(LocalDateTime.now());
        
        record = loanRecordRepository.save(record);
        return toResponse(record);
    }
    
    @Transactional
    public LoanRecordDto.Response reject(Long id, Long approverId, String reason) {
        LoanRecord record = loanRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借用记录不存在: " + id));
        
        if (record.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("只有待处理的申请可以驳回");
        }
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("审批人不存在: " + approverId));
        
        record.setApprovalStatus(ApprovalStatus.REJECTED);
        record.setApprovedBy(approver);
        record.setApprovedAt(LocalDateTime.now());
        record.setRejectionReason(reason);
        
        record = loanRecordRepository.save(record);
        return toResponse(record);
    }
    
    @Transactional
    public LoanRecordDto.Response returnDevice(Long id) {
        LoanRecord record = loanRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借用记录不存在: " + id));
        
        if (record.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new RuntimeException("只有已批准的借用可以归还");
        }
        
        if (record.getActualReturnDate() != null) {
            throw new RuntimeException("设备已归还，请勿重复操作");
        }
        
        // Get device with pessimistic lock
        Device device = deviceRepository.findByIdWithLock(record.getDevice().getId())
                .orElseThrow(() -> new RuntimeException("设备不存在"));
        
        // Update device status
        device.setIsLoaned(false);
        device.setCurrentHolder(null);
        device.setLoanDate(null);
        deviceRepository.save(device);
        
        // Update loan record
        LocalDateTime now = LocalDateTime.now();
        record.setActualReturnDate(now);
        
        // Determine return status
        if (now.toLocalDate().isAfter(record.getExpectedReturnDate())) {
            record.setReturnStatus(ReturnStatus.OVERDUE);
        } else {
            record.setReturnStatus(ReturnStatus.NORMAL);
        }
        
        record = loanRecordRepository.save(record);
        return toResponse(record);
    }
    
    private LoanRecordDto.Response toResponse(LoanRecord record) {
        return LoanRecordDto.Response.builder()
                .id(record.getId())
                .deviceId(record.getDevice().getId())
                .deviceName(record.getDevice().getName())
                .applicantId(record.getApplicant().getId())
                .applicantName(record.getApplicant().getUsername())
                .applicantStaffId(record.getApplicant().getStaffId())
                .applyDate(record.getApplyDate())
                .expectedReturnDate(record.getExpectedReturnDate())
                .actualReturnDate(record.getActualReturnDate())
                .approvalStatus(record.getApprovalStatus())
                .returnStatus(record.getReturnStatus())
                .purpose(record.getPurpose())
                .approvedByName(record.getApprovedBy() != null ? record.getApprovedBy().getUsername() : null)
                .approvedAt(record.getApprovedAt())
                .rejectionReason(record.getRejectionReason())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
