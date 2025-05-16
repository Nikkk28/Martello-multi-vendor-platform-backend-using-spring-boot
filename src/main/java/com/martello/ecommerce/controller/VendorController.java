package com.martello.ecommerce.controller;

import com.martello.ecommerce.model.dto.*;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.model.enums.OrderStatus;
import com.martello.ecommerce.service.OrderService;
import com.martello.ecommerce.service.ProductService;
import com.martello.ecommerce.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final ProductService productService;
    private final OrderService orderService;
    private final VendorService vendorService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<VendorDashboardResponse>> getDashboard(@AuthenticationPrincipal User user) {
        VendorDashboardResponse dashboard = vendorService.getVendorDashboard(user);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getVendorProducts(@AuthenticationPrincipal User user) {
        List<ProductResponse> products = productService.getVendorProducts(user);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal User user) {
        ProductResponse product = productService.createProduct(request, user);
        return ResponseEntity.ok(ApiResponse.success(product, "Product created successfully"));
    }

    @PatchMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal User user) {
        ProductResponse product = productService.updateProduct(id, request, user);
        return ResponseEntity.ok(ApiResponse.success(product, "Product updated successfully"));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        productService.deleteProduct(id, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getVendorOrders(@AuthenticationPrincipal User user) {
        List<OrderResponse> orders = orderService.getVendorOrders(user);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            @AuthenticationPrincipal User user) {
        OrderResponse order = orderService.updateOrderStatus(id, status, user);
        return ResponseEntity.ok(ApiResponse.success(order, "Order status updated successfully"));
    }
}
