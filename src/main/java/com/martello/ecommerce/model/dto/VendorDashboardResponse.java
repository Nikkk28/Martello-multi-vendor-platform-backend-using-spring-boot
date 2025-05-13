package com.martello.ecommerce.model.dto;

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
    private List<Map<String, Object>> topProducts;
    private List<BigDecimal> weeklySalesTrend;
}
