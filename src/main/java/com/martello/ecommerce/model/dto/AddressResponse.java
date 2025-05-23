package com.martello.ecommerce.model.dto;

import com.martello.ecommerce.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phoneNumber;
    private String fullName;
    private Boolean isDefault;
    private Boolean isShippingAddress;
    private Boolean isBillingAddress;
    private User user;
}
