package com.dms.controller;

import com.dms.dto.LoanRecordDto;
import com.dms.entity.LoanRecord.ApprovalStatus;
import com.dms.entity.User;
import com.dms.service.LoanRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanRecordController {
    
    private final LoanRecordService loanRecordService;
    
    @GetMapping
    public ResponseEntity<Page<LoanRecordDto.Response>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ApprovalStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(loanRecordService.findAll(search, status, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LoanRecordDto.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(loanRecordService.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<LoanRecordDto.Response> apply(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody LoanRecordDto.ApplyRequest request) {
        return ResponseEntity.ok(loanRecordService.apply(user.getId(), request));
    }
    
    @PutMapping("/{id}/approve")
    public ResponseEntity<LoanRecordDto.Response> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(loanRecordService.approve(id, user.getId()));
    }
    
    @PutMapping("/{id}/reject")
    public ResponseEntity<LoanRecordDto.Response> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestBody(required = false) LoanRecordDto.ApproveRequest request) {
        String reason = request != null ? request.getRejectionReason() : null;
        return ResponseEntity.ok(loanRecordService.reject(id, user.getId(), reason));
    }
    
    @PostMapping("/{id}/return")
    public ResponseEntity<LoanRecordDto.Response> returnDevice(@PathVariable Long id) {
        return ResponseEntity.ok(loanRecordService.returnDevice(id));
    }
}
