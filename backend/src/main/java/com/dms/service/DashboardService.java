package com.dms.service;

import com.dms.dto.DashboardDto;
import com.dms.dto.DeviceStatusDto;
import com.dms.entity.LoanRecord.ApprovalStatus;
import com.dms.repository.DeviceRepository;
import com.dms.repository.LoanRecordRepository;
import com.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final LoanRecordRepository loanRecordRepository;
    private final JdbcTemplate jdbcTemplate;

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

    @Transactional(readOnly = true)
    public List<DeviceStatusDto> getStatusDistributionFromProcedure() {
        // Call stored procedure sp_get_status_distribution and map results to DTO
        String sql = "CALL sp_get_status_distribution()";
        List<DeviceStatusDto> list = jdbcTemplate.query(sql, new RowMapper<DeviceStatusDto>() {
            @Override
            public DeviceStatusDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new DeviceStatusDto(rs.getString("device_status"), rs.getLong("cnt"));
            }
        });
        return list;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStatusDistributionMap() {
        List<DeviceStatusDto> list = getStatusDistributionFromProcedure();
        return list.stream().collect(Collectors.toMap(DeviceStatusDto::getDeviceStatus, DeviceStatusDto::getCnt));
    }
}
