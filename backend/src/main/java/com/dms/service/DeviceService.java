package com.dms.service;

import com.dms.dto.DeviceDto;
import com.dms.entity.Device;
import com.dms.repository.DeviceRepository;
import com.dms.repository.LoanRecordRepository;
import com.dms.entity.LoanRecord.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final LoanRecordRepository loanRecordRepository;
    
    @Transactional(readOnly = true)
    public Page<DeviceDto.Response> findAll(String search, String status, Boolean isLoaned, Pageable pageable) {
        return deviceRepository.findByFilters(search, status, isLoaned, pageable)
                .map(this::toResponse);
    }
    
    @Transactional(readOnly = true)
    public DeviceDto.Response findById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备不存在: " + id));
        return toResponse(device);
    }
    
    @Transactional
    public DeviceDto.Response create(DeviceDto.Request request) {
        Device device = Device.builder()
                .name(request.getName())
                .type(request.getType())
                .location(request.getLocation())
                .purchaseDate(request.getPurchaseDate())
                .status(request.getStatus() != null ? request.getStatus() : "正常")
                .isLoaned(false)
                .build();
        
        device = deviceRepository.save(device);
        return toResponse(device);
    }
    
    @Transactional
    public DeviceDto.Response update(Long id, DeviceDto.Request request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备不存在: " + id));
        
        device.setName(request.getName());
        device.setType(request.getType());
        device.setLocation(request.getLocation());
        device.setPurchaseDate(request.getPurchaseDate());
        if (request.getStatus() != null) {
            device.setStatus(request.getStatus());
        }
        
        device = deviceRepository.save(device);
        return toResponse(device);
    }
    
    @Transactional
    public void delete(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备不存在: " + id));
        
        // Check if device has active loans
        boolean hasActiveLoans = loanRecordRepository.existsByDeviceIdAndApprovalStatusAndActualReturnDateIsNull(
                id, ApprovalStatus.APPROVED);
        if (hasActiveLoans) {
            throw new RuntimeException("设备有未归还的借用记录，无法删除");
        }
        
        deviceRepository.delete(device);
    }
    
    private DeviceDto.Response toResponse(Device device) {
        return DeviceDto.Response.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getType())
                .location(device.getLocation())
                .purchaseDate(device.getPurchaseDate())
                .isLoaned(device.getIsLoaned())
                .status(device.getStatus())
                .loanDate(device.getLoanDate())
                .currentHolderName(device.getCurrentHolder() != null ? device.getCurrentHolder().getUsername() : null)
                .currentHolderId(device.getCurrentHolder() != null ? device.getCurrentHolder().getId() : null)
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}
