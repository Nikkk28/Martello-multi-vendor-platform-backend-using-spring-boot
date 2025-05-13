package com.martello.ecommerce.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    
    @NotEmpty(message = "Order must contain at least one item")
    private List<@Valid OrderItemRequest> items;
    
    private String shippingAddress;
    
    private String billingAddress;
}

