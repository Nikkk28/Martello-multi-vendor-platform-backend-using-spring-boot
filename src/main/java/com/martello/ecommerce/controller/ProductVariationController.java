package com.martello.ecommerce.controller;

import com.martello.ecommerce.model.dto.ApiResponse;
import com.martello.ecommerce.model.dto.ProductVariationRequest;
import com.martello.ecommerce.model.dto.ProductVariationResponse;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.service.ProductVariationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/variations")
@RequiredArgsConstructor
public class ProductVariationController {

    private final ProductVariationService productVariationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductVariationResponse>>> getProductVariations(
            @PathVariable Long productId) {
        List<ProductVariationResponse> variations = productVariationService.getProductVariations(productId);
        return ResponseEntity.ok(ApiResponse.success(variations));
    }

    @GetMapping("/{variationId}")
    public ResponseEntity<ApiResponse<ProductVariationResponse>> getProductVariation(
            @PathVariable Long productId,
            @PathVariable Long variationId) {
        ProductVariationResponse variation = productVariationService.getProductVariation(productId, variationId);
        return ResponseEntity.ok(ApiResponse.success(variation));
    }

    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<ProductVariationResponse>> createProductVariation(
            @PathVariable Long productId,
            @Valid @RequestBody ProductVariationRequest request,
            @AuthenticationPrincipal User user) {
        ProductVariationResponse variation = productVariationService.createProductVariation(productId, request, user);
        return ResponseEntity.ok(ApiResponse.success(variation, "Product variation created successfully"));
    }

    @PutMapping("/{variationId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<ProductVariationResponse>> updateProductVariation(
            @PathVariable Long productId,
            @PathVariable Long variationId,
            @Valid @RequestBody ProductVariationRequest request,
            @AuthenticationPrincipal User user) {
        ProductVariationResponse variation = productVariationService.updateProductVariation(
                productId, variationId, request, user);
        return ResponseEntity.ok(ApiResponse.success(variation, "Product variation updated successfully"));
    }

    @DeleteMapping("/{variationId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> deleteProductVariation(
            @PathVariable Long productId,
            @PathVariable Long variationId,
            @AuthenticationPrincipal User user) {
        productVariationService.deleteProductVariation(productId, variationId, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Product variation deleted successfully"));
    }
}
