package com.martello.ecommerce.controller;

import com.martello.ecommerce.model.dto.AddressRequest;
import com.martello.ecommerce.model.dto.AddressResponse;
import com.martello.ecommerce.model.dto.ApiResponse;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getUserAddresses(@AuthenticationPrincipal User user) {
        List<AddressResponse> addresses = addressService.getUserAddresses(user);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        AddressResponse address = addressService.getAddressById(id, user);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal User user) {
        AddressResponse address = addressService.createAddress(request, user);
        return ResponseEntity.ok(ApiResponse.success(address, "Address created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal User user) {
        AddressResponse address = addressService.updateAddress(id, request, user);
        return ResponseEntity.ok(ApiResponse.success(address, "Address updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        addressService.deleteAddress(id, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Address deleted successfully"));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        AddressResponse address = addressService.setDefaultAddress(id, user);
        return ResponseEntity.ok(ApiResponse.success(address, "Default address updated successfully"));
    }
}
