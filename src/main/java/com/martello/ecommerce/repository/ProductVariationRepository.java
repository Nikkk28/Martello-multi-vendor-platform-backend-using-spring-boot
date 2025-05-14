package com.martello.ecommerce.repository;

import com.martello.ecommerce.model.entity.ProductVariation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariationRepository extends JpaRepository<ProductVariation, Long> {
    List<ProductVariation> findByProductId(Long productId);
    Optional<ProductVariation> findByProductIdAndSku(Long productId, String sku);
    List<ProductVariation> findByProductIdAndIsActiveTrue(Long productId);
}
