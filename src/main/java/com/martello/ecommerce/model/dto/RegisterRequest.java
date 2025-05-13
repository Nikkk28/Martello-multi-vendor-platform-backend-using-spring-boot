package com.martello.ecommerce.model.dto;

import com.martello.ecommerce.model.enums.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
public class RegisterRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotNull(message = "Role is required")
    private Role role;
    
    @Valid
    private VendorProfileRequest vendorProfile;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorProfileRequest {
        
        @NotBlank(message = "Business name is required")
        private String businessName;
        
        @NotBlank(message = "Business description is required")
        private String businessDescription;
        
        @NotBlank(message = "Contact phone is required")
        private String contactPhone;
    }
}
