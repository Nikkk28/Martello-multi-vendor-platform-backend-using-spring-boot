package com.martello.ecommerce.repository;

import com.martello.ecommerce.model.entity.Order;
import com.martello.ecommerce.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByVendorId(Long vendorId);
    List<Order> findByVendorIdAndStatus(Long vendorId, OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate")
    Long countOrdersSince(LocalDateTime startDate);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.vendor.id = :vendorId")
    BigDecimal sumTotalAmountByVendor(Long vendorId);
    
    @Query("SELECT DATE(o.createdAt) as date, SUM(o.totalAmount) as total FROM Order o " +
           "WHERE o.vendor.id = :vendorId AND o.createdAt >= :startDate " +
           "GROUP BY DATE(o.createdAt) ORDER BY date")
    List<Object[]> findWeeklySalesTrendByVendor(Long vendorId, LocalDateTime startDate);
}
