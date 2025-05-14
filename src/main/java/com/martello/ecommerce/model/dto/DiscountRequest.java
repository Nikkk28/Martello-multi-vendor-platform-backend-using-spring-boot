package com.martello.ecommerce.model.dto;

import com.martello.ecommerce.model.enums.DiscountType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class DiscountRequest {
    
    @NotBlank(message = "Discount code is required")
    private String code;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Discount type is required")
    private DiscountType type;
    
    @NotNull(message = "Discount value is required")
    @Min(value = 0, message = "Discount value must be greater than or equal to 0")
    private BigDecimal value;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;
    
    @NotNull(message = "Usage limit is required")
    @Min(value = 0, message = "Usage limit must be greater than or equal to 0")
    private Integer usageLimit;
    
    private Long categoryId;
    
    private Long productId;
    
    private Long vendorId;
    
    private BigDecimal minimumOrderAmount;
    
    private Boolean isActive;
}
