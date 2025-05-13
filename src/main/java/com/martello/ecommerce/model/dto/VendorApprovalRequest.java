package com.martello.ecommerce.model.dto;

import com.martello.ecommerce.model.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorApprovalRequest {
    
    @NotNull(message = "Status is required")
    private ApprovalStatus status;
    
    private String reason;
}
