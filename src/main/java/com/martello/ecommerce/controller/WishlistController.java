package com.martello.ecommerce.controller;

import com.martello.ecommerce.model.dto.ApiResponse;
import com.martello.ecommerce.model.dto.WishlistRequest;
import com.martello.ecommerce.model.dto.WishlistResponse;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistResponse>>> getUserWishlists(@AuthenticationPrincipal User user) {
        List<WishlistResponse> wishlists = wishlistService.getUserWishlists(user);
        return ResponseEntity.ok(ApiResponse.success(wishlists));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlistById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        WishlistResponse wishlist = wishlistService.getWishlistById(id, user);
        return ResponseEntity.ok(ApiResponse.success(wishlist));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WishlistResponse>> createWishlist(
            @Valid @RequestBody WishlistRequest request,
            @AuthenticationPrincipal User user) {
        WishlistResponse wishlist = wishlistService.createWishlist(request, user);
        return ResponseEntity.ok(ApiResponse.success(wishlist, "Wishlist created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WishlistResponse>> updateWishlist(
            @PathVariable Long id,
            @Valid @RequestBody WishlistRequest request,
            @AuthenticationPrincipal User user) {
        WishlistResponse wishlist = wishlistService.updateWishlist(id, request, user);
        return ResponseEntity.ok(ApiResponse.success(wishlist, "Wishlist updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWishlist(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        wishlistService.deleteWishlist(id, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Wishlist deleted successfully"));
    }

    @PostMapping("/{wishlistId}/products/{productId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> addProductToWishlist(
            @PathVariable Long wishlistId,
            @PathVariable Long productId,
            @AuthenticationPrincipal User user) {
        WishlistResponse wishlist = wishlistService.addProductToWishlist(wishlistId, productId, user);
        return ResponseEntity.ok(ApiResponse.success(wishlist, "Product added to wishlist successfully"));
    }

    @DeleteMapping("/{wishlistId}/products/{productId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> removeProductFromWishlist(
            @PathVariable Long wishlistId,
            @PathVariable Long productId,
            @AuthenticationPrincipal User user) {
        WishlistResponse wishlist = wishlistService.removeProductFromWishlist(wishlistId, productId, user);
        return ResponseEntity.ok(ApiResponse.success(wishlist, "Product removed from wishlist successfully"));
    }

    @GetMapping("/products/{productId}/public")
    public ResponseEntity<ApiResponse<List<WishlistResponse>>> getPublicWishlistsForProduct(
            @PathVariable Long productId) {
        List<WishlistResponse> wishlists = wishlistService.getPublicWishlistsForProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(wishlists));
    }
}
