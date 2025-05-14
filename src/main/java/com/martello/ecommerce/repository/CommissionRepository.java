package com.martello.ecommerce.repository;

import com.martello.ecommerce.model.entity.Commission;
import com.martello.ecommerce.model.enums.CommissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {
    Page<Commission> findByVendorId(Long vendorId, Pageable pageable);
    List<Commission> findByVendorIdAndStatus(Long vendorId, CommissionStatus status);
    
    @Query("SELECT SUM(c.commissionAmount) FROM Commission c WHERE c.vendor.id = :vendorId AND c.status = :status")
    BigDecimal sumCommissionAmountByVendorAndStatus(Long vendorId, CommissionStatus status);
    
    @Query("SELECT SUM(c.vendorEarnings) FROM Commission c WHERE c.vendor.id = :vendorId AND c.status = :status")
    BigDecimal sumVendorEarningsByVendorAndStatus(Long vendorId, CommissionStatus status);
    
    @Query("SELECT SUM(c.commissionAmount) FROM Commission c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumCommissionAmountBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
}
