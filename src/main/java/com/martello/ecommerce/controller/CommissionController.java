package com.martello.ecommerce.controller;

import com.martello.ecommerce.model.dto.ApiResponse;
import com.martello.ecommerce.model.dto.CommissionRateRequest;
import com.martello.ecommerce.model.dto.CommissionRateResponse;
import com.martello.ecommerce.model.dto.CommissionResponse;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.service.CommissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class CommissionController {

    private final CommissionService commissionService;

    @GetMapping("/vendor/commissions")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<CommissionResponse>>> getVendorCommissions(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        Page<CommissionResponse> commissions = commissionService.getVendorCommissions(user, pageable);
        return ResponseEntity.ok(ApiResponse.success(commissions));
    }

    @GetMapping("/vendor/commissions/pending")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<BigDecimal>> getVendorPendingCommissions(@AuthenticationPrincipal User user) {
        BigDecimal pendingAmount = commissionService.getVendorPendingCommissions(user);
        return ResponseEntity.ok(ApiResponse.success(pendingAmount));
    }

    @PostMapping("/admin/commissions/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> processCommissionPayment(@PathVariable Long id) {
        commissionService.processCommissionPayment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Commission payment processed successfully"));
    }

    @PostMapping("/admin/commission-rates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CommissionRateResponse>> setCommissionRate(
            @Valid @RequestBody CommissionRateRequest request) {
        CommissionRateResponse rate = commissionService.setCommissionRate(request);
        return ResponseEntity.ok(ApiResponse.success(rate, "Commission rate set successfully"));
    }

    @GetMapping("/admin/commission-rates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CommissionRateResponse>>> getCommissionRates(
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long categoryId) {
        List<CommissionRateResponse> rates = commissionService.getCommissionRates(vendorId, categoryId);
        return ResponseEntity.ok(ApiResponse.success(rates));
    }
}
