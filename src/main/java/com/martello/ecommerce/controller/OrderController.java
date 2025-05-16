package com.martello.ecommerce.controller;

import com.martello.ecommerce.model.dto.ApiResponse;
import com.martello.ecommerce.model.dto.OrderRequest;
import com.martello.ecommerce.model.dto.OrderResponse;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal User user) {
        OrderResponse order = orderService.createOrder(request, user);
        return ResponseEntity.ok(ApiResponse.success(order, "Order created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getCustomerOrders(@AuthenticationPrincipal User user) {
        List<OrderResponse> orders = orderService.getCustomerOrders(user);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
}
