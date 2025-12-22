package com.dms.controller;

import com.dms.dto.DashboardDto;
import com.dms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/statistics")
    public ResponseEntity<DashboardDto> getStatistics() {
        return ResponseEntity.ok(dashboardService.getStatistics());
    }

    @GetMapping("/status-distribution")
    public ResponseEntity<Map<String, Long>> getStatusDistribution() {
        return ResponseEntity.ok(dashboardService.getStatusDistributionMap());
    }
}
