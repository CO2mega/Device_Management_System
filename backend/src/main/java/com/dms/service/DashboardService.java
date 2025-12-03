package com.dms.service;

import com.dms.dto.DashboardDto;
import com.dms.entity.LoanRecord.ApprovalStatus;
import com.dms.repository.DeviceRepository;
import com.dms.repository.LoanRecordRepository;
import com.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final LoanRecordRepository loanRecordRepository;
    
    @Transactional(readOnly = true)
    public DashboardDto getStatistics() {
        long totalDevices = deviceRepository.count();
        long loanedDevices = deviceRepository.countByIsLoaned(true);
        long availableDevices = totalDevices - loanedDevices;
        long totalUsers = userRepository.count();
        long pendingLoans = loanRecordRepository.countByApprovalStatus(ApprovalStatus.PENDING);
        long approvedLoans = loanRecordRepository.countByApprovalStatus(ApprovalStatus.APPROVED);
        long rejectedLoans = loanRecordRepository.countByApprovalStatus(ApprovalStatus.REJECTED);
        
        return DashboardDto.builder()
                .totalDevices(totalDevices)
                .loanedDevices(loanedDevices)
                .availableDevices(availableDevices)
                .totalUsers(totalUsers)
                .pendingLoans(pendingLoans)
                .approvedLoans(approvedLoans)
                .rejectedLoans(rejectedLoans)
                .build();
    }
}
