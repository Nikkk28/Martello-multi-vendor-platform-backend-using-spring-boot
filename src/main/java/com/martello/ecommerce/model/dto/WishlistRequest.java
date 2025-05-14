package com.martello.ecommerce.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistRequest {
    
    @NotBlank(message = "Wishlist name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Public status is required")
    private Boolean isPublic;
}
