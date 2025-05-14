package com.martello.ecommerce.repository;

import com.martello.ecommerce.model.entity.CommissionRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRateRepository extends JpaRepository<CommissionRate, Long> {
    Optional<CommissionRate> findByVendorIdAndCategoryIdAndIsActiveTrue(Long vendorId, Long categoryId);
    Optional<CommissionRate> findByVendorIdAndCategoryIdIsNullAndIsActiveTrue(Long vendorId);
    Optional<CommissionRate> findByCategoryIdAndVendorIdIsNullAndIsActiveTrue(Long categoryId);
    List<CommissionRate> findByVendorIdAndIsActiveTrue(Long vendorId);
    List<CommissionRate> findByCategoryIdAndIsActiveTrue(Long categoryId);
}
