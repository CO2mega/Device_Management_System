package com.dms.controller;

import com.dms.dto.DeviceDto;
import com.dms.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    
    private final DeviceService deviceService;
    
    @GetMapping
    public ResponseEntity<Page<DeviceDto.Response>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isLoaned,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(deviceService.findAll(search, status, isLoaned, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDto.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<DeviceDto.Response> create(@Valid @RequestBody DeviceDto.Request request) {
        return ResponseEntity.ok(deviceService.create(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDto.Response> update(@PathVariable Long id, 
                                                      @Valid @RequestBody DeviceDto.Request request) {
        return ResponseEntity.ok(deviceService.update(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
