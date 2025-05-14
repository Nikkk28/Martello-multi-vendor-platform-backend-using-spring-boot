package com.martello.ecommerce.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long id;
    private Long userId;
    private List<VendorGroup> vendorGroups;
    private Integer totalItems;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorGroup {
        private Long vendorId;
        private String vendorName;
        private BigDecimal subtotal;
        private List<CartItemDto> items;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDto {
        private Long id;
        private Long productId;
        private String productName;
        private Long variationId;
        private Map<String, String> variationAttributes;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
        private String imageUrl;
    }
}
