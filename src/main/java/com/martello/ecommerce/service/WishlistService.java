package com.martello.ecommerce.service;

import com.martello.ecommerce.exception.BadRequestException;
import com.martello.ecommerce.exception.ResourceNotFoundException;
import com.martello.ecommerce.model.dto.WishlistRequest;
import com.martello.ecommerce.model.dto.WishlistResponse;
import com.martello.ecommerce.model.entity.Product;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.model.entity.Wishlist;
import com.martello.ecommerce.repository.ProductRepository;
import com.martello.ecommerce.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<WishlistResponse> getUserWishlists(User user) {
        List<Wishlist> wishlists = wishlistRepository.findByUserId(user.getId());
        return wishlists.stream()
                .map(this::mapToWishlistResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WishlistResponse getWishlistById(Long wishlistId, User user) {
        Wishlist wishlist = getWishlistForUser(wishlistId, user);
        return mapToWishlistResponse(wishlist);
    }

    @Transactional
    public WishlistResponse createWishlist(WishlistRequest request, User user) {
        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .isPublic(request.getIsPublic())
                .build();
        
        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        return mapToWishlistResponse(savedWishlist);
    }

    @Transactional
    public WishlistResponse updateWishlist(Long wishlistId, WishlistRequest request, User user) {
        Wishlist wishlist = getWishlistForUser(wishlistId, user);
        
        wishlist.setName(request.getName());
        wishlist.setDescription(request.getDescription());
        wishlist.setIsPublic(request.getIsPublic());
        
        Wishlist updatedWishlist = wishlistRepository.save(wishlist);
        return mapToWishlistResponse(updatedWishlist);
    }

    @Transactional
    public void deleteWishlist(Long wishlistId, User user) {
        Wishlist wishlist = getWishlistForUser(wishlistId, user);
        wishlistRepository.delete(wishlist);
    }

    @Transactional
    public WishlistResponse addProductToWishlist(Long wishlistId, Long productId, User user) {
        Wishlist wishlist = getWishlistForUser(wishlistId, user);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Check if product is already in wishlist
        if (wishlist.getProducts().stream().anyMatch(p -> p.getId().equals(productId))) {
            throw new BadRequestException("Product is already in this wishlist");
        }
        
        wishlist.getProducts().add(product);
        Wishlist updatedWishlist = wishlistRepository.save(wishlist);
        
        return mapToWishlistResponse(updatedWishlist);
    }

    @Transactional
    public WishlistResponse removeProductFromWishlist(Long wishlistId, Long productId, User user) {
        Wishlist wishlist = getWishlistForUser(wishlistId, user);
        
        // Check if product is in wishlist
        if (wishlist.getProducts().stream().noneMatch(p -> p.getId().equals(productId))) {
            throw new BadRequestException("Product is not in this wishlist");
        }
        
        wishlist.getProducts().removeIf(p -> p.getId().equals(productId));
        Wishlist updatedWishlist = wishlistRepository.save(wishlist);
        
        return mapToWishlistResponse(updatedWishlist);
    }

    @Transactional(readOnly = true)
    public List<WishlistResponse> getPublicWishlistsForProduct(Long productId) {
        List<Wishlist> wishlists = wishlistRepository.findByIsPublicTrueAndProductsId(productId);
        return wishlists.stream()
                .map(this::mapToWishlistResponse)
                .collect(Collectors.toList());
    }

    private Wishlist getWishlistForUser(Long wishlistId, User user) {
        return wishlistRepository.findByUserIdAndId(user.getId(), wishlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));
    }

    private WishlistResponse mapToWishlistResponse(Wishlist wishlist) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser().getId())
                .userName(wishlist.getUser().getFirstName() + " " + wishlist.getUser().getLastName())
                .name(wishlist.getName())
                .description(wishlist.getDescription())
                .isPublic(wishlist.getIsPublic())
                .products(wishlist.getProducts().stream()
                        .map(product -> WishlistResponse.ProductDto.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .price(product.getPrice())
                                .imageUrl(product.getImageUrls() != null && !product.getImageUrls().isEmpty() 
                                        ? product.getImageUrls().get(0) : null)
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(wishlist.getCreatedAt())
                .updatedAt(wishlist.getUpdatedAt())
                .build();
    }
}
