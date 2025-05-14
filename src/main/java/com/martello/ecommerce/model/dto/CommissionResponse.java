package com.martello.ecommerce.model.dto;

import com.martello.ecommerce.model.enums.CommissionStatus;
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
public class CommissionResponse {
    private Long id;
    private Long orderId;
    private Long vendorId;
    private String vendorName;
    private BigDecimal orderAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal vendorEarnings;
    private CommissionStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
