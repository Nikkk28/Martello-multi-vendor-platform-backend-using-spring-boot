package com.martello.ecommerce.repository;

import com.martello.ecommerce.model.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductIdAndProductVariationId(Long cartId, Long productId, Long variationId);
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
