package com.martello.ecommerce.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRateRequest {
    
    private Long vendorId;
    
    private Long categoryId;
    
    @NotNull(message = "Commission rate is required")
    @Min(value = 0, message = "Commission rate must be at least 0")
    @Max(value = 100, message = "Commission rate must be at most 100")
    private BigDecimal rate;
    
    private String description;
}
