package com.martello.ecommerce.service;

import com.martello.ecommerce.exception.BadRequestException;
import com.martello.ecommerce.exception.ResourceNotFoundException;
import com.martello.ecommerce.model.dto.OrderItemRequest;
import com.martello.ecommerce.model.dto.OrderRequest;
import com.martello.ecommerce.model.dto.OrderResponse;
import com.martello.ecommerce.model.entity.*;
import com.martello.ecommerce.model.enums.OrderStatus;
import com.martello.ecommerce.repository.OrderRepository;
import com.martello.ecommerce.repository.ProductRepository;
import com.martello.ecommerce.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final NotificationService notificationService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, User customer) {
        // Validate all products exist and belong to the same vendor
        List<OrderItemRequest> items = request.getItems();
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }
        
        // Get all product IDs
        List<Long> productIds = items.stream()
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toList());
        
        // Fetch all products at once
        List<Product> products = productRepository.findAllById(productIds);
        
        if (products.size() != productIds.size()) {
            throw new ResourceNotFoundException("One or more products not found");
        }
        
        // Check if all products belong to the same vendor
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        
        Long vendorId = null;
        for (OrderItemRequest item : items) {
            Product product = productMap.get(item.getProductId());
            
            if (vendorId == null) {
                vendorId = product.getVendor().getId();
            } else if (!vendorId.equals(product.getVendor().getId())) {
                throw new BadRequestException("All products must belong to the same vendor");
            }
            
            // Check stock
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Not enough stock for product: " + product.getName());
            }
        }
        
        // Get the vendor
        VendorProfile vendor = vendorProfileRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        
        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        // Create the order
        Order order = Order.builder()
                .customer(customer)
                .vendor(vendor)
                .totalAmount(totalAmount) // Will update after calculating
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .build();
        
        Order savedOrder = orderRepository.save(order);
        
        // Create order items and update product stock
        for (OrderItemRequest itemRequest : items) {
            Product product = productMap.get(itemRequest.getProductId());
            
            // Create order item
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();
            
            orderItems.add(orderItem);
            
            // Update total amount
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            
            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }
        
        // Update order with total amount and items
        savedOrder.setTotalAmount(totalAmount);
        savedOrder.setItems(orderItems);
        Order finalOrder = orderRepository.save(savedOrder);
        
        // Notify vendor about new order
        notificationService.sendNotification(
                vendor.getUser(),
                "New Order Received",
                "You have received a new order (#" + finalOrder.getId() + ") worth " + totalAmount
        );
        
        return mapToOrderResponse(finalOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(User customer) {
        List<Order> orders = orderRepository.findByCustomerId(customer.getId());
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getVendorOrders(User user) {
        VendorProfile vendorProfile = vendorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
        
        List<Order> orders = orderRepository.findByVendorId(vendorProfile.getId());
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Verify ownership (vendor only)
        VendorProfile vendorProfile = vendorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
        
        if (!order.getVendor().getId().equals(vendorProfile.getId())) {
            throw new BadRequestException("You don't have permission to update this order");
        }
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        // Notify customer about order status change
        notificationService.sendNotification(
                order.getCustomer(),
                "Order Status Updated",
                "Your order (#" + order.getId() + ") status has been updated to " + status
        );
        
        return mapToOrderResponse(updatedOrder);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName())
                .vendorId(order.getVendor().getId())
                .vendorName(order.getVendor().getBusinessName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(order.getItems().stream()
                        .map(item -> OrderResponse.OrderItemDto.builder()
                                .id(item.getId())
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .quantity(item.getQuantity())
                                .price(item.getPriceAtPurchase())
                                .build())
                        .collect(Collectors.toList()))
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
