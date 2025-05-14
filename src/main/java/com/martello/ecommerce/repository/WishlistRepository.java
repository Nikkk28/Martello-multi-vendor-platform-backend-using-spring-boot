package com.martello.ecommerce.repository;

import com.martello.ecommerce.model.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserId(Long userId);
    Optional<Wishlist> findByUserIdAndId(Long userId, Long wishlistId);
    List<Wishlist> findByIsPublicTrueAndProductsId(Long productId);
}
