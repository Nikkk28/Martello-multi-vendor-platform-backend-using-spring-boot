package com.martello.ecommerce.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariationRequest {
    
    @NotBlank(message = "SKU is required")
    private String sku;
    
    @NotNull(message = "Attributes are required")
    private Map<String, String> attributes;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    private Integer stockQuantity;
    
    private BigDecimal priceAdjustment;
    
    @NotNull(message = "Active status is required")
    private Boolean isActive;
}
