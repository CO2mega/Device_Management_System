package com.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private long totalDevices;
    private long loanedDevices;
    private long availableDevices;
    private long totalUsers;
    private long pendingLoans;
    private long approvedLoans;
    private long rejectedLoans;
}
