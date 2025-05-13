package com.martello.ecommerce.service;

import com.martello.ecommerce.exception.BadRequestException;
import com.martello.ecommerce.exception.ResourceNotFoundException;
import com.martello.ecommerce.model.dto.VendorApprovalRequest;
import com.martello.ecommerce.model.dto.VendorDashboardResponse;
import com.martello.ecommerce.model.entity.Order;
import com.martello.ecommerce.model.entity.Product;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.model.entity.VendorProfile;
import com.martello.ecommerce.model.enums.ApprovalStatus;
import com.martello.ecommerce.model.enums.OrderStatus;
import com.martello.ecommerce.repository.OrderRepository;
import com.martello.ecommerce.repository.ProductRepository;
import com.martello.ecommerce.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorProfileRepository vendorProfileRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<VendorProfile> getPendingVendors() {
        return vendorProfileRepository.findByStatus(ApprovalStatus.PENDING);
    }

    @Transactional
    public VendorProfile updateVendorStatus(Long vendorId, VendorApprovalRequest request) {
        VendorProfile vendorProfile = vendorProfileRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        
        vendorProfile.setStatus(request.getStatus());
        
        if (request.getStatus() == ApprovalStatus.REJECTED) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new BadRequestException("Rejection reason is required");
            }
            vendorProfile.setRejectionReason(request.getReason());
            
            // Notify vendor about rejection
            notificationService.sendNotification(
                    vendorProfile.getUser(),
                    "Vendor Application Rejected",
                    "Your vendor application has been rejected. Reason: " + request.getReason()
            );
        } else if (request.getStatus() == ApprovalStatus.APPROVED) {
            // Auto-approve all pending products
            List<Product> products = productRepository.findByVendorId(vendorId);
            products.forEach(product -> product.setIsListed(true));
            productRepository.saveAll(products);
            
            // Notify vendor about approval
            notificationService.sendNotification(
                    vendorProfile.getUser(),
                    "Welcome to Martello!",
                    "Your vendor application has been approved. You can now start adding products."
            );
        }
        
        return vendorProfileRepository.save(vendorProfile);
    }

    @Transactional(readOnly = true)
    public VendorDashboardResponse getVendorDashboard(User user) {
        VendorProfile vendorProfile = vendorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
        
        // Calculate total earnings
        BigDecimal totalEarnings = orderRepository.sumTotalAmountByVendor(vendorProfile.getId());
        if (totalEarnings == null) {
            totalEarnings = BigDecimal.ZERO;
        }
        
        // Get pending orders count
        List<Order> pendingOrders = orderRepository.findByVendorIdAndStatus(vendorProfile.getId(), OrderStatus.PENDING);
        
        // Get top products
        List<Product> topProducts = productRepository.findTopProductsByVendor(
                vendorProfile.getId(), 
                PageRequest.of(0, 5)
        );
        
        // Get weekly sales trend
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> weeklySalesTrend = orderRepository.findWeeklySalesTrendByVendor(vendorProfile.getId(), weekAgo);
        
        // Convert to response format
        List<Map<String, Object>> topProductsList = topProducts.stream()
                .map(product -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", product.getName());
                    map.put("sales", product.getOrderItems().size());
                    return map;
                })
                .collect(Collectors.toList());
        
        List<BigDecimal> salesTrend = new ArrayList<>();
        if (weeklySalesTrend != null && !weeklySalesTrend.isEmpty()) {
            salesTrend = weeklySalesTrend.stream()
                    .map(row -> (BigDecimal) row[1])
                    .collect(Collectors.toList());
        }
        
        return VendorDashboardResponse.builder()
                .totalEarnings(totalEarnings)
                .pendingOrders(pendingOrders.size())
                .topProducts(topProductsList)
                .weeklySalesTrend(salesTrend)
                .build();
    }
}
