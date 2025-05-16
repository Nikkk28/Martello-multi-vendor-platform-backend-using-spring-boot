package com.martello.ecommerce.service;

import com.martello.ecommerce.exception.BadRequestException;
import com.martello.ecommerce.exception.ResourceNotFoundException;
import com.martello.ecommerce.model.dto.AuthRequest;
import com.martello.ecommerce.model.dto.AuthResponse;
import com.martello.ecommerce.model.dto.RefreshTokenRequest;
import com.martello.ecommerce.model.dto.RegisterRequest;
import com.martello.ecommerce.model.entity.RefreshToken;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.model.entity.VendorProfile;
import com.martello.ecommerce.model.enums.ApprovalStatus;
import com.martello.ecommerce.model.enums.Role;
import com.martello.ecommerce.repository.RefreshTokenRepository;
import com.martello.ecommerce.repository.UserRepository;
import com.martello.ecommerce.repository.VendorProfileRepository;
import com.martello.ecommerce.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final NotificationService notificationService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .enabled(true)
                .addresses(request.getAddress())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .phoneNumber(request.getPhoneNumber())
                .build();
        if (request.getAddress() != null) {
            request.getAddress().forEach(address -> address.setUser(user));
            user.setAddresses(request.getAddress());
        }

        User savedUser = userRepository.save(user);

        if (request.getRole() == Role.VENDOR && request.getVendorProfile() != null) {
            VendorProfile vendorProfile = VendorProfile.builder()
                    .user(savedUser)
                    .businessName(request.getVendorProfile().getBusinessName())
                    .businessDescription(request.getVendorProfile().getBusinessDescription())
                    .contactPhone(request.getVendorProfile().getContactPhone())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .status(ApprovalStatus.PENDING)
                    .build();
            
            vendorProfileRepository.save(vendorProfile);
            
            // Notify admins about new vendor registration
            notificationService.notifyAdmins(
                    "New Vendor Registration",
                    "A new vendor '" + vendorProfile.getBusinessName() + "' has registered and is awaiting approval."
            );
        }

        String accessToken = jwtService.generateToken(savedUser);
        RefreshToken refreshToken = createRefreshToken(savedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadRequestException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        String accessToken = jwtService.generateToken(user);
        System.out.println("Received refresh token: " + request.getRefreshToken());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        
        refreshTokenRepository.delete(token);
    }

    private RefreshToken createRefreshToken(User user) {
        // Delete any existing refresh tokens for this user
        refreshTokenRepository.deleteByUserId(user.getId());
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        
        return refreshTokenRepository.save(refreshToken);
    }
}
