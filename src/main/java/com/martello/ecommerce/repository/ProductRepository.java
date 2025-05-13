package com.martello.ecommerce.repository;

import com.martello.ecommerce.model.entity.Product;
import com.martello.ecommerce.model.entity.VendorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByIsListedTrue(Pageable pageable);
    Page<Product> findByVendorAndIsListedTrue(VendorProfile vendor, Pageable pageable);
    List<Product> findByVendorId(Long vendorId);
    
    @Query("SELECT p FROM Product p WHERE p.vendor.id = :vendorId ORDER BY SIZE(p.orderItems) DESC")
    List<Product> findTopProductsByVendor(Long vendorId, Pageable pageable);
    
    @Query("SELECT p.category.name, COUNT(p) FROM Product p GROUP BY p.category.name")
    List<Object[]> countProductsByCategory();
}
