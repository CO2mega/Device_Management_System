package com.dms.repository;

import com.dms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByStaffId(String staffId);
    
    Optional<User> findByUsername(String username);
    
    boolean existsByStaffId(String staffId);
    
    boolean existsByStaffIdAndIdNot(String staffId, Long id);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR u.staffId LIKE %:search% OR u.username LIKE %:search%) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> findByFilters(@Param("search") String search,
                             @Param("role") String role,
                             @Param("status") String status,
                             Pageable pageable);
    
    long countByRole(String role);
}
