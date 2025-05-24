package com.martello.ecommerce.model.dto;

import com.martello.ecommerce.model.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDashboardResponse {
    private BigDecimal totalEarnings;
    private Integer pendingOrders;
    private int totalProducts;
    private List<Map<String, Object>> topProducts;
    private List<WeeklySalesTrend> weeklySalesTrend;
    private ApprovalStatus approvalStatus;
}
