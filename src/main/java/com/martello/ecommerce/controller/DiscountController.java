package com.martello.ecommerce.controller;

import com.martello.ecommerce.model.dto.ApiResponse;
import com.martello.ecommerce.model.dto.DiscountRequest;
import com.martello.ecommerce.model.dto.DiscountResponse;
import com.martello.ecommerce.service.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DiscountResponse>>> getAllActiveDiscounts() {
        List<DiscountResponse> discounts = discountService.getAllActiveDiscounts();
        return ResponseEntity.ok(ApiResponse.success(discounts));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<DiscountResponse>> getDiscountByCode(@PathVariable String code) {
        DiscountResponse discount = discountService.getDiscountByCode(code);
        return ResponseEntity.ok(ApiResponse.success(discount));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DiscountResponse>> createDiscount(@Valid @RequestBody DiscountRequest request) {
        DiscountResponse discount = discountService.createDiscount(request);
        return ResponseEntity.ok(ApiResponse.success(discount, "Discount created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DiscountResponse>> updateDiscount(
            @PathVariable Long id,
            @Valid @RequestBody DiscountRequest request) {
        DiscountResponse discount = discountService.updateDiscount(id, request);
        return ResponseEntity.ok(ApiResponse.success(discount, "Discount updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Discount deleted successfully"));
    }

    @GetMapping("/applicable")
    public ResponseEntity<ApiResponse<List<DiscountResponse>>> getApplicableDiscounts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long productId) {
        List<DiscountResponse> discounts = discountService.getApplicableDiscounts(categoryId, vendorId, productId);
        return ResponseEntity.ok(ApiResponse.success(discounts));
    }
}
