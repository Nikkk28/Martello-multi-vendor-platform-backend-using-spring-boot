package com.martello.ecommerce.model.dto;

import com.martello.ecommerce.model.entity.VendorProfile;
import com.martello.ecommerce.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private VendorProfile vendorProfile;
    private LocalDateTime createdAt;
}
