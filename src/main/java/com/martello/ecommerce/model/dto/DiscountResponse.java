package com.martello.ecommerce.model.dto;

import com.martello.ecommerce.model.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResponse {
    private Long id;
    private String code;
    private String description;
    private DiscountType type;
    private BigDecimal value;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usageCount;
    private Long categoryId;
    private String categoryName;
    private Long productId;
    private String productName;
    private Long vendorId;
    private String vendorName;
    private BigDecimal minimumOrderAmount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
