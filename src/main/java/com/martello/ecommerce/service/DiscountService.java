package com.martello.ecommerce.service;

import com.martello.ecommerce.exception.BadRequestException;
import com.martello.ecommerce.exception.ResourceNotFoundException;
import com.martello.ecommerce.model.dto.DiscountRequest;
import com.martello.ecommerce.model.dto.DiscountResponse;
import com.martello.ecommerce.model.entity.Category;
import com.martello.ecommerce.model.entity.Discount;
import com.martello.ecommerce.model.entity.Product;
import com.martello.ecommerce.model.entity.VendorProfile;
import com.martello.ecommerce.repository.CategoryRepository;
import com.martello.ecommerce.repository.DiscountRepository;
import com.martello.ecommerce.repository.ProductRepository;
import com.martello.ecommerce.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final VendorProfileRepository vendorProfileRepository;

    @Transactional(readOnly = true)
    public List<DiscountResponse> getAllActiveDiscounts() {
        LocalDateTime now = LocalDateTime.now();
        List<Discount> discounts = discountRepository.findAll().stream()
                .filter(d -> d.getIsActive() && d.getStartDate().isBefore(now) && d.getEndDate().isAfter(now))
                .collect(Collectors.toList());
        
        return discounts.stream()
                .map(this::mapToDiscountResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DiscountResponse getDiscountByCode(String code) {
        Discount discount = discountRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new ResourceNotFoundException("Discount code not found or inactive"));
        
        // Validate if discount is currently active
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(discount.getStartDate()) || now.isAfter(discount.getEndDate())) {
            throw new BadRequestException("Discount code is not currently active");
        }
        
        // Check usage limit
        if (discount.getUsageLimit() > 0 && discount.getUsageCount() >= discount.getUsageLimit()) {
            throw new BadRequestException("Discount code has reached its usage limit");
        }
        
        return mapToDiscountResponse(discount);
    }

    @Transactional
    public DiscountResponse createDiscount(DiscountRequest request) {
        // Validate code uniqueness
        if (discountRepository.findByCodeAndIsActiveTrue(request.getCode()).isPresent()) {
            throw new BadRequestException("Discount code already exists");
        }
        
        // Validate dates
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }
        
        // Validate references
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }
        
        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        }
        
        VendorProfile vendor = null;
        if (request.getVendorId() != null) {
            vendor = vendorProfileRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        }
        
        Discount discount = Discount.builder()
                .code(request.getCode())
                .description(request.getDescription())
                .type(request.getType())
                .value(request.getValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .usageLimit(request.getUsageLimit())
                .usageCount(0)
                .category(category)
                .product(product)
                .vendor(vendor)
                .minimumOrderAmount(request.getMinimumOrderAmount() != null ? 
                        request.getMinimumOrderAmount() : BigDecimal.ZERO)
                .isActive(true)
                .build();
        
        Discount savedDiscount = discountRepository.save(discount);
        return mapToDiscountResponse(savedDiscount);
    }

    @Transactional
    public DiscountResponse updateDiscount(Long id, DiscountRequest request) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
        
        // Validate code uniqueness if changed
        if (!discount.getCode().equals(request.getCode()) && 
            discountRepository.findByCodeAndIsActiveTrue(request.getCode()).isPresent()) {
            throw new BadRequestException("Discount code already exists");
        }
        
        // Validate dates
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }
        
        // Validate references
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }
        
        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        }
        
        VendorProfile vendor = null;
        if (request.getVendorId() != null) {
            vendor = vendorProfileRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        }
        
        discount.setCode(request.getCode());
        discount.setDescription(request.getDescription());
        discount.setType(request.getType());
        discount.setValue(request.getValue());
        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());
        discount.setUsageLimit(request.getUsageLimit());
        discount.setCategory(category);
        discount.setProduct(product);
        discount.setVendor(vendor);
        discount.setMinimumOrderAmount(request.getMinimumOrderAmount() != null ? 
                request.getMinimumOrderAmount() : BigDecimal.ZERO);
        discount.setIsActive(request.getIsActive());
        
        Discount updatedDiscount = discountRepository.save(discount);
        return mapToDiscountResponse(updatedDiscount);
    }

    @Transactional
    public void deleteDiscount(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
        
        // Soft delete by setting isActive to false
        discount.setIsActive(false);
        discountRepository.save(discount);
    }

    @Transactional
    public void incrementDiscountUsage(String code) {
        Discount discount = discountRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new ResourceNotFoundException("Discount code not found or inactive"));
        
        discount.setUsageCount(discount.getUsageCount() + 1);
        discountRepository.save(discount);
    }

    @Transactional(readOnly = true)
    public List<DiscountResponse> getApplicableDiscounts(Long categoryId, Long vendorId, Long productId) {
        LocalDateTime now = LocalDateTime.now();
        List<Discount> discounts = new java.util.ArrayList<>();
        
        // Get product-specific discounts
        if (productId != null) {
            discounts.addAll(discountRepository.findValidDiscountsByProduct(productId, now));
        }
        
        // Get category and vendor discounts
        if (categoryId != null && vendorId != null) {
            discounts.addAll(discountRepository.findValidDiscountsByCategoryAndVendor(categoryId, vendorId, now));
        }
        
        return discounts.stream()
                .distinct() // Remove duplicates
                .map(this::mapToDiscountResponse)
                .collect(Collectors.toList());
    }

    private DiscountResponse mapToDiscountResponse(Discount discount) {
        return DiscountResponse.builder()
                .id(discount.getId())
                .code(discount.getCode())
                .description(discount.getDescription())
                .type(discount.getType())
                .value(discount.getValue())
                .startDate(discount.getStartDate())
                .endDate(discount.getEndDate())
                .usageLimit(discount.getUsageLimit())
                .usageCount(discount.getUsageCount())
                .categoryId(discount.getCategory() != null ? discount.getCategory().getId() : null)
                .categoryName(discount.getCategory() != null ? discount.getCategory().getName() : null)
                .productId(discount.getProduct() != null ? discount.getProduct().getId() : null)
                .productName(discount.getProduct() != null ? discount.getProduct().getName() : null)
                .vendorId(discount.getVendor() != null ? discount.getVendor().getId() : null)
                .vendorName(discount.getVendor() != null ? discount.getVendor().getBusinessName() : null)
                .minimumOrderAmount(discount.getMinimumOrderAmount())
                .isActive(discount.getIsActive())
                .createdAt(discount.getCreatedAt())
                .updatedAt(discount.getUpdatedAt())
                .build();
    }
}
