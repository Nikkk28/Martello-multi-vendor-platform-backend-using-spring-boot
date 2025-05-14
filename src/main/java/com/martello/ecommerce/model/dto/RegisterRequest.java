package com.martello.ecommerce.model.dto;

import com.martello.ecommerce.model.entity.Address;
import com.martello.ecommerce.model.enums.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @NotNull(message = "Address is required")
    private List<Address> address;

    @NotNull(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;
    
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
