package com.martello.ecommerce.controller;

import com.martello.ecommerce.model.dto.AdminDashboardResponse;
import com.martello.ecommerce.model.dto.ApiResponse;
import com.martello.ecommerce.model.dto.VendorApprovalRequest;
import com.martello.ecommerce.model.entity.VendorProfile;
import com.martello.ecommerce.service.AdminService;
import com.martello.ecommerce.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final VendorService vendorService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        AdminDashboardResponse dashboard = adminService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/vendors/pending")
    public ResponseEntity<ApiResponse<List<VendorProfile>>> getPendingVendors() {
        List<VendorProfile> vendors = vendorService.getPendingVendors();
        return ResponseEntity.ok(ApiResponse.success(vendors));
    }

    @PatchMapping("/vendors/{id}/status")
    public ResponseEntity<ApiResponse<VendorProfile>> updateVendorStatus(
            @PathVariable Long id,
            @Valid @RequestBody VendorApprovalRequest request) {
        VendorProfile vendor = vendorService.updateVendorStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(vendor, "Vendor status updated successfully"));
    }
}
