package com.martello.ecommerce.repository;

import com.martello.ecommerce.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductIdAndIsApprovedTrue(Long productId, Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
    Double getAverageRatingForProduct(Long productId);
    
    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);
    
    Page<Review> findByIsApprovedFalse(Pageable pageable);
}
