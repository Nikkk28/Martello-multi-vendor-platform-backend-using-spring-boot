package com.martello.ecommerce.service;

import com.martello.ecommerce.exception.ResourceNotFoundException;
import com.martello.ecommerce.model.dto.UserDto;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.model.entity.VendorProfile;
import com.martello.ecommerce.model.enums.Role;
import com.martello.ecommerce.repository.UserRepository;
import com.martello.ecommerce.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUserProfile(User user) {
        UserDto userDto = mapToUserDto(user);
        
        if (user.getRole() == Role.VENDOR) {
            VendorProfile vendorProfile = vendorProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
            userDto.setVendorProfile(vendorProfile);
        }
        
        return userDto;
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
