package com.dms.controller;

import com.dms.dto.UserDto;
import com.dms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<Page<UserDto.Response>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(search, role, status, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDto.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<UserDto.Response> create(@Valid @RequestBody UserDto.Request request) {
        return ResponseEntity.ok(userService.create(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDto.Response> update(@PathVariable Long id, 
                                                    @Valid @RequestBody UserDto.Request request) {
        return ResponseEntity.ok(userService.update(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
