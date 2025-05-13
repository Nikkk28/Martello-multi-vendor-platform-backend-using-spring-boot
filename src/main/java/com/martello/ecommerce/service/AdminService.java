package com.martello.ecommerce.service;

import com.martello.ecommerce.model.dto.AdminDashboardResponse;
import com.martello.ecommerce.model.entity.VendorProfile;
import com.martello.ecommerce.model.enums.ApprovalStatus;
import com.martello.ecommerce.repository.OrderRepository;
import com.martello.ecommerce.repository.ProductRepository;
import com.martello.ecommerce.repository.UserRepository;
import com.martello.ecommerce.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        // Get total users
        long totalUsers = userRepository.count();
        
        // Get pending approvals
        List<VendorProfile> pendingVendors = vendorProfileRepository.findByStatus(ApprovalStatus.PENDING);
        
        // Get daily transactions
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Long dailyTransactions = orderRepository.countOrdersSince(today);
        
        // Get revenue by category
        List<Object[]> productsByCategory = productRepository.countProductsByCategory();
        Map<String, Long> revenueByCategory = new HashMap<>();
        
        for (Object[] row : productsByCategory) {
            String category = (String) row[0];
            Long count = (Long) row[1];
            revenueByCategory.put(category, count);
        }
        
        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .pendingApprovals(pendingVendors.size())
                .dailyTransactions(dailyTransactions)
                .revenueByCategory(revenueByCategory)
                .build();
    }
}
