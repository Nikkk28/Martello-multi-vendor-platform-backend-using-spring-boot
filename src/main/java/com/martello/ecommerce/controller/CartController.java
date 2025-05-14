package com.martello.ecommerce.controller;

import com.martello.ecommerce.model.dto.ApiResponse;
import com.martello.ecommerce.model.dto.CartItemRequest;
import com.martello.ecommerce.model.dto.CartResponse;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getUserCart(@AuthenticationPrincipal User user) {
        CartResponse cart = cartService.getUserCart(user);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(
            @Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal User user) {
        CartResponse cart = cartService.addItemToCart(request, user);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item added to cart successfully"));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal User user) {
        CartResponse cart = cartService.updateCartItem(itemId, request, user);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart item updated successfully"));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal User user) {
        CartResponse cart = cartService.removeCartItem(itemId, user);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item removed from cart successfully"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(@AuthenticationPrincipal User user) {
        CartResponse cart = cartService.clearCart(user);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart cleared successfully"));
    }
}
