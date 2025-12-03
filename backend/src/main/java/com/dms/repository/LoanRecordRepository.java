package com.dms.repository;

import com.dms.entity.LoanRecord;
import com.dms.entity.LoanRecord.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRecordRepository extends JpaRepository<LoanRecord, Long> {
    
    @Query("SELECT lr FROM LoanRecord lr " +
           "JOIN FETCH lr.device d " +
           "JOIN FETCH lr.applicant a " +
           "WHERE (:search IS NULL OR d.name LIKE %:search% OR a.username LIKE %:search%) AND " +
           "(:status IS NULL OR lr.approvalStatus = :status)")
    Page<LoanRecord> findByFilters(@Param("search") String search,
                                   @Param("status") ApprovalStatus status,
                                   Pageable pageable);
    
    @Query("SELECT lr FROM LoanRecord lr WHERE lr.device.id = :deviceId AND lr.approvalStatus = 'APPROVED' AND lr.actualReturnDate IS NULL")
    List<LoanRecord> findActiveLoansForDevice(@Param("deviceId") Long deviceId);
    
    @Query("SELECT lr FROM LoanRecord lr WHERE lr.applicant.id = :userId AND lr.actualReturnDate IS NULL")
    List<LoanRecord> findActiveLoansForUser(@Param("userId") Long userId);
    
    long countByApprovalStatus(ApprovalStatus status);
    
    @Query("SELECT COUNT(lr) FROM LoanRecord lr WHERE lr.approvalStatus = 'APPROVED' AND lr.actualReturnDate IS NULL")
    long countActiveLoanedDevices();
    
    boolean existsByDeviceIdAndApprovalStatusAndActualReturnDateIsNull(Long deviceId, ApprovalStatus status);
}
