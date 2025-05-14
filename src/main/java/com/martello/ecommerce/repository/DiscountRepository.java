package com.martello.ecommerce.repository;

import com.martello.ecommerce.model.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Optional<Discount> findByCodeAndIsActiveTrue(String code);
    
    @Query("SELECT d FROM Discount d WHERE d.isActive = true AND d.startDate <= :now AND d.endDate >= :now " +
           "AND (d.usageLimit > d.usageCount OR d.usageLimit = 0) " +
           "AND (d.category.id = :categoryId OR d.category IS NULL) " +
           "AND (d.vendor.id = :vendorId OR d.vendor IS NULL)")
    List<Discount> findValidDiscountsByCategoryAndVendor(Long categoryId, Long vendorId, LocalDateTime now);
    
    @Query("SELECT d FROM Discount d WHERE d.isActive = true AND d.startDate <= :now AND d.endDate >= :now " +
           "AND (d.usageLimit > d.usageCount OR d.usageLimit = 0) " +
           "AND d.product.id = :productId")
    List<Discount> findValidDiscountsByProduct(Long productId, LocalDateTime now);
}
