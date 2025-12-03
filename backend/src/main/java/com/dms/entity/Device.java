package com.dms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_device")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long id;
    
    @Column(name = "device_name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "device_type", length = 50)
    private String type;
    
    @Column(name = "location", length = 200)
    private String location;
    
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    
    @Column(name = "is_loaned", nullable = false)
    @Builder.Default
    private Boolean isLoaned = false;
    
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "正常";
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_holder_id")
    private User currentHolder;
    
    @Column(name = "loan_date")
    private LocalDate loanDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
