package com.dms.service;

import com.dms.dto.UserDto;
import com.dms.entity.User;
import com.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional(readOnly = true)
    public Page<UserDto.Response> findAll(String search, String role, String status, Pageable pageable) {
        return userRepository.findByFilters(search, role, status, pageable)
                .map(this::toResponse);
    }
    
    @Transactional(readOnly = true)
    public UserDto.Response findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
        return toResponse(user);
    }
    
    @Transactional(readOnly = true)
    public UserDto.Response findByStaffId(String staffId) {
        User user = userRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + staffId));
        return toResponse(user);
    }
    
    @Transactional
    public UserDto.Response create(UserDto.Request request) {
        if (userRepository.existsByStaffId(request.getStaffId())) {
            throw new RuntimeException("工号已存在: " + request.getStaffId());
        }
        
        User user = User.builder()
                .staffId(request.getStaffId())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : "USER")
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        
        user = userRepository.save(user);
        return toResponse(user);
    }
    
    @Transactional
    public UserDto.Response update(Long id, UserDto.Request request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
        
        if (!user.getStaffId().equals(request.getStaffId()) && 
            userRepository.existsByStaffIdAndIdNot(request.getStaffId(), id)) {
            throw new RuntimeException("工号已存在: " + request.getStaffId());
        }
        
        user.setStaffId(request.getStaffId());
        user.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        
        user = userRepository.save(user);
        return toResponse(user);
    }
    
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
        userRepository.delete(user);
    }
    
    private UserDto.Response toResponse(User user) {
        return UserDto.Response.builder()
                .id(user.getId())
                .staffId(user.getStaffId())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
