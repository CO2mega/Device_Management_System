package com.dms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "t_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    
    @Column(name = "staff_id", unique = true, nullable = false, length = 50)
    private String staffId;
    
    @Column(name = "username", nullable = false, length = 100)
    private String username;
    
    @Column(name = "password", nullable = false, length = 200)
    private String password;
    
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private String role = "USER";
    
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return !"LOCKED".equals(status);
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(status);
    }
}
