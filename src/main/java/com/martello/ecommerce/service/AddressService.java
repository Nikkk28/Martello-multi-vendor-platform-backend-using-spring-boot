package com.martello.ecommerce.service;

import com.martello.ecommerce.exception.BadRequestException;
import com.martello.ecommerce.exception.ResourceNotFoundException;
import com.martello.ecommerce.model.dto.AddressRequest;
import com.martello.ecommerce.model.dto.AddressResponse;
import com.martello.ecommerce.model.entity.Address;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(User user) {
        List<Address> addresses = addressRepository.findByUserId(user.getId());
        return addresses.stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long addressId, User user) {
        Address address = addressRepository.findByUserIdAndId(user.getId(), addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        
        return mapToAddressResponse(address);
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request, User user) {
        Address address = Address.builder()
                .user(user)
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName())
                .isShippingAddress(request.getIsShippingAddress())
                .isBillingAddress(request.getIsBillingAddress())
                .isDefault(request.getIsDefault())
                .build();
        
        // If this is set as default, unset any existing default address
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(defaultAddress -> {
                        defaultAddress.setIsDefault(false);
                        addressRepository.save(defaultAddress);
                    });
        }
        
        Address savedAddress = addressRepository.save(address);
        return mapToAddressResponse(savedAddress);
    }

    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest request, User user) {
        Address address = addressRepository.findByUserIdAndId(user.getId(), addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setFullName(request.getFullName());
        address.setIsShippingAddress(request.getIsShippingAddress());
        address.setIsBillingAddress(request.getIsBillingAddress());
        
        // Handle default address change
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(defaultAddress -> {
                        defaultAddress.setIsDefault(false);
                        addressRepository.save(defaultAddress);
                    });
            address.setIsDefault(true);
        }
        
        Address updatedAddress = addressRepository.save(address);
        return mapToAddressResponse(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long addressId, User user) {
        Address address = addressRepository.findByUserIdAndId(user.getId(), addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            throw new BadRequestException("Cannot delete default address. Please set another address as default first.");
        }
        
        addressRepository.delete(address);
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long addressId, User user) {
        Address newDefaultAddress = addressRepository.findByUserIdAndId(user.getId(), addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        
        // Unset current default address
        addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .ifPresent(currentDefault -> {
                    currentDefault.setIsDefault(false);
                    addressRepository.save(currentDefault);
                });
        
        // Set new default
        newDefaultAddress.setIsDefault(true);
        Address savedAddress = addressRepository.save(newDefaultAddress);
        
        return mapToAddressResponse(savedAddress);
    }

    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .phoneNumber(address.getPhoneNumber())
                .fullName(address.getFullName())
                .isDefault(address.getIsDefault())
                .isShippingAddress(address.getIsShippingAddress())
                .isBillingAddress(address.getIsBillingAddress())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
