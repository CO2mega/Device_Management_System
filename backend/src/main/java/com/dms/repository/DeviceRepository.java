package com.dms.repository;

import com.dms.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    
    @Query("SELECT d FROM Device d WHERE " +
           "(:search IS NULL OR d.name LIKE %:search% OR d.type LIKE %:search% OR d.location LIKE %:search%) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:isLoaned IS NULL OR d.isLoaned = :isLoaned)")
    Page<Device> findByFilters(@Param("search") String search,
                               @Param("status") String status,
                               @Param("isLoaned") Boolean isLoaned,
                               Pageable pageable);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Device d WHERE d.id = :id")
    Optional<Device> findByIdWithLock(@Param("id") Long id);
    
    long countByIsLoaned(Boolean isLoaned);
    
    boolean existsByNameAndIdNot(String name, Long id);
}
