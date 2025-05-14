package com.martello.ecommerce.service;

import com.martello.ecommerce.exception.BadRequestException;
import com.martello.ecommerce.exception.ResourceNotFoundException;
import com.martello.ecommerce.model.dto.CommissionRateRequest;
import com.martello.ecommerce.model.dto.CommissionRateResponse;
import com.martello.ecommerce.model.dto.CommissionResponse;
import com.martello.ecommerce.model.entity.*;
import com.martello.ecommerce.model.enums.CommissionStatus;
import com.martello.ecommerce.repository.CategoryRepository;
import com.martello.ecommerce.repository.CommissionRateRepository;
import com.martello.ecommerce.repository.CommissionRepository;
import com.martello.ecommerce.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommissionService {

    private final CommissionRepository commissionRepository;
    private final CommissionRateRepository commissionRateRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;

    @Transactional
    public Commission calculateAndSaveCommission(Order order) {
        VendorProfile vendor = order.getVendor();
        BigDecimal orderAmount = order.getTotalAmount();
        
        // Get applicable commission rate
        BigDecimal commissionRate = getCommissionRate(vendor, order);
        
        // Calculate commission amount
        BigDecimal commissionAmount = orderAmount.multiply(commissionRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // Calculate vendor earnings
        BigDecimal vendorEarnings = orderAmount.subtract(commissionAmount);
        
        // Create and save commission record
        Commission commission = Commission.builder()
                .order(order)
                .vendor(vendor)
                .orderAmount(orderAmount)
                .commissionRate(commissionRate)
                .commissionAmount(commissionAmount)
                .vendorEarnings(vendorEarnings)
                .status(CommissionStatus.PENDING)
                .build();
        
        return commissionRepository.save(commission);
    }

    private BigDecimal getCommissionRate(VendorProfile vendor, Order order) {
        // Default commission rate if no specific rates are found
        BigDecimal defaultRate = BigDecimal.valueOf(10.0);
        
        // Try to find vendor-specific rate for the category of the first product
        if (!order.getItems().isEmpty()) {
            Product firstProduct = order.getItems().get(0).getProduct();
            Category category = firstProduct.getCategory();
            
            if (category != null) {
                // Try vendor-specific rate for this category
                Optional<CommissionRate> vendorCategoryRate = 
                        commissionRateRepository.findByVendorIdAndCategoryIdAndIsActiveTrue(
                                vendor.getId(), category.getId());
                
                if (vendorCategoryRate.isPresent()) {
                    return vendorCategoryRate.get().getRate();
                }
                
                // Try category-specific rate (for all vendors)
                Optional<CommissionRate> categoryRate = 
                        commissionRateRepository.findByCategoryIdAndVendorIdIsNullAndIsActiveTrue(
                                category.getId());
                
                if (categoryRate.isPresent()) {
                    return categoryRate.get().getRate();
                }
            }
        }
        
        // Try vendor-specific default rate
        Optional<CommissionRate> vendorDefaultRate = 
                commissionRateRepository.findByVendorIdAndCategoryIdIsNullAndIsActiveTrue(
                        vendor.getId());
        
        return vendorDefaultRate.map(CommissionRate::getRate).orElse(defaultRate);
    }

    @Transactional(readOnly = true)
    public Page<CommissionResponse> getVendorCommissions(User user, Pageable pageable) {
        VendorProfile vendorProfile = vendorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
        
        Page<Commission> commissions = commissionRepository.findByVendorId(vendorProfile.getId(), pageable);
        return commissions.map(this::mapToCommissionResponse);
    }

    @Transactional(readOnly = true)
    public BigDecimal getVendorPendingCommissions(User user) {
        VendorProfile vendorProfile = vendorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
        
        BigDecimal pendingAmount = commissionRepository.sumVendorEarningsByVendorAndStatus(
                vendorProfile.getId(), CommissionStatus.PENDING);
        
        return pendingAmount != null ? pendingAmount : BigDecimal.ZERO;
    }

    @Transactional
    public void processCommissionPayment(Long commissionId) {
        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found"));
        
        if (commission.getStatus() != CommissionStatus.PENDING) {
            throw new BadRequestException("Commission is not in PENDING status");
        }
        
        commission.setStatus(CommissionStatus.PROCESSING);
        commissionRepository.save(commission);
        
        // In a real system, this would trigger payment processing
        // For now, we'll just simulate it by updating the status
        
        commission.setStatus(CommissionStatus.PAID);
        commission.setPaidAt(LocalDateTime.now());
        commissionRepository.save(commission);
        
        // Notify vendor about payment
        notificationService.sendNotification(
                commission.getVendor().getUser(),
                "Commission Payment Processed",
                "Your commission payment of $" + commission.getVendorEarnings() + 
                " for order #" + commission.getOrder().getId() + " has been processed."
        );
    }

    @Transactional
    public CommissionRateResponse setCommissionRate(CommissionRateRequest request) {
        // Validate inputs
        VendorProfile vendor = null;
        if (request.getVendorId() != null) {
            vendor = vendorProfileRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        }
        
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }
        
        // Find existing rate or create new one
        CommissionRate commissionRate;
        if (vendor != null && category != null) {
            commissionRate = commissionRateRepository
                    .findByVendorIdAndCategoryIdAndIsActiveTrue(vendor.getId(), category.getId())
                    .orElse(new CommissionRate());
        } else if (vendor != null) {
            commissionRate = commissionRateRepository
                    .findByVendorIdAndCategoryIdIsNullAndIsActiveTrue(vendor.getId())
                    .orElse(new CommissionRate());
        } else if (category != null) {
            commissionRate = commissionRateRepository
                    .findByCategoryIdAndVendorIdIsNullAndIsActiveTrue(category.getId())
                    .orElse(new CommissionRate());
        } else {
            throw new BadRequestException("Either vendorId or categoryId must be provided");
        }
        
        // Update or set new values
        commissionRate.setVendor(vendor);
        commissionRate.setCategory(category);
        commissionRate.setRate(request.getRate());
        commissionRate.setDescription(request.getDescription());
        commissionRate.setIsActive(true);
        
        CommissionRate savedRate = commissionRateRepository.save(commissionRate);
        
        // Notify vendor if applicable
        if (vendor != null) {
            String message = category != null ?
                    "Commission rate for category '" + category.getName() + "' has been updated to " + request.getRate() + "%" :
                    "Your default commission rate has been updated to " + request.getRate() + "%";
            
            notificationService.sendNotification(
                    vendor.getUser(),
                    "Commission Rate Updated",
                    message
            );
        }
        
        return mapToCommissionRateResponse(savedRate);
    }

    @Transactional(readOnly = true)
    public List<CommissionRateResponse> getCommissionRates(Long vendorId, Long categoryId) {
        List<CommissionRate> rates;
        
        if (vendorId != null && categoryId != null) {
            // Get specific vendor-category rate
            rates = commissionRateRepository.findByVendorIdAndCategoryIdAndIsActiveTrue(vendorId, categoryId)
                    .map(List::of).orElse(List.of());
        } else if (vendorId != null) {
            // Get all rates for vendor
            rates = commissionRateRepository.findByVendorIdAndIsActiveTrue(vendorId);
        } else if (categoryId != null) {
            // Get all rates for category
            rates = commissionRateRepository.findByCategoryIdAndIsActiveTrue(categoryId);
        } else {
            // Get all global rates (no vendor, no category)
            rates = commissionRateRepository.findAll().stream()
                    .filter(CommissionRate::getIsActive)
                    .collect(Collectors.toList());
        }
        
        return rates.stream()
                .map(this::mapToCommissionRateResponse)
                .collect(Collectors.toList());
    }

    private CommissionResponse mapToCommissionResponse(Commission commission) {
        return CommissionResponse.builder()
                .id(commission.getId())
                .orderId(commission.getOrder().getId())
                .vendorId(commission.getVendor().getId())
                .vendorName(commission.getVendor().getBusinessName())
                .orderAmount(commission.getOrderAmount())
                .commissionRate(commission.getCommissionRate())
                .commissionAmount(commission.getCommissionAmount())
                .vendorEarnings(commission.getVendorEarnings())
                .status(commission.getStatus())
                .paidAt(commission.getPaidAt())
                .createdAt(commission.getCreatedAt())
                .updatedAt(commission.getUpdatedAt())
                .build();
    }

    private CommissionRateResponse mapToCommissionRateResponse(CommissionRate rate) {
        return CommissionRateResponse.builder()
                .id(rate.getId())
                .vendorId(rate.getVendor() != null ? rate.getVendor().getId() : null)
                .vendorName(rate.getVendor() != null ? rate.getVendor().getBusinessName() : null)
                .categoryId(rate.getCategory() != null ? rate.getCategory().getId() : null)
                .categoryName(rate.getCategory() != null ? rate.getCategory().getName() : null)
                .rate(rate.getRate())
                .description(rate.getDescription())
                .isActive(rate.getIsActive())
                .createdAt(rate.getCreatedAt())
                .updatedAt(rate.getUpdatedAt())
                .build();
    }
}
