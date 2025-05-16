package com.martello.ecommerce.service;

import com.martello.ecommerce.exception.BadRequestException;
import com.martello.ecommerce.exception.ResourceNotFoundException;
import com.martello.ecommerce.model.dto.CartItemRequest;
import com.martello.ecommerce.model.dto.CartResponse;
import com.martello.ecommerce.model.entity.*;
import com.martello.ecommerce.repository.CartItemRepository;
import com.martello.ecommerce.repository.CartRepository;
import com.martello.ecommerce.repository.ProductRepository;
import com.martello.ecommerce.repository.ProductVariationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariationRepository productVariationRepository;

    @Transactional
    public CartResponse getUserCart(User user) {
        Cart cart = getOrCreateCart(user);
        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse addItemToCart(CartItemRequest request, User user) {
        Cart cart = getOrCreateCart(user);
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Check if product is listed
        if (!product.getIsListed()) {
            throw new BadRequestException("Product is not available for purchase");
        }
        
        // Check stock
        int availableStock = product.getStockQuantity();
        
        ProductVariation variation = null;
        if (request.getVariationId() != null) {
            variation = productVariationRepository.findById(request.getVariationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product variation not found"));
            
            // Verify variation belongs to product
            if (!variation.getProduct().getId().equals(product.getId())) {
                throw new BadRequestException("Variation does not belong to this product");
            }
            
            // Check if variation is active
            if (!variation.getIsActive()) {
                throw new BadRequestException("Product variation is not available");
            }
            
            // Use variation stock
            availableStock = variation.getStockQuantity();
        }
        
        // Check if requested quantity is available
        if (request.getQuantity() > availableStock) {
            throw new BadRequestException("Not enough stock available. Only " + availableStock + " items left.");
        }
        
        // Check if item already exists in cart
        CartItem cartItem;
        if (variation != null) {
            cartItem = cartItemRepository.findByCartIdAndProductIdAndProductVariationId(
                    cart.getId(), product.getId(), variation.getId()).orElse(null);
        } else {
            cartItem = cartItemRepository.findByCartIdAndProductId(
                    cart.getId(), product.getId()).orElse(null);
        }
        
        if (cartItem != null) {
            // Update existing item
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (newQuantity > availableStock) {
                throw new BadRequestException("Cannot add more items. Only " + availableStock + " items available.");
            }
            cartItem.setQuantity(newQuantity);
        } else {
            // Create new item
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .productVariation(variation)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(cartItem);
        }
        
        cartItemRepository.save(cartItem);
        Cart updatedCart = cartRepository.save(cart);
        
        return mapToCartResponse(updatedCart);
    }

    @Transactional
    public CartResponse updateCartItem(Long itemId, CartItemRequest request, User user) {
        Cart cart = getOrCreateCart(user);
        
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        // Verify item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }
        
        // Check stock
        int availableStock;
        if (cartItem.getProductVariation() != null) {
            availableStock = cartItem.getProductVariation().getStockQuantity();
        } else {
            availableStock = cartItem.getProduct().getStockQuantity();
        }
        
        if (request.getQuantity() > availableStock) {
            throw new BadRequestException("Not enough stock available. Only " + availableStock + " items left.");
        }
        
        // Update quantity
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        
        Cart updatedCart = cartRepository.save(cart);
        return mapToCartResponse(updatedCart);
    }

    @Transactional
    public CartResponse removeCartItem(Long itemId, User user) {
        Cart cart = getOrCreateCart(user);
        
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        // Verify item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }
        
        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        
        Cart updatedCart = cartRepository.save(cart);
        return mapToCartResponse(updatedCart);
    }

    @Transactional
    public CartResponse clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        
        // Remove all items
        cart.getItems().clear();
        cartRepository.save(cart);
        
        return mapToCartResponse(cart);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse mapToCartResponse(Cart cart) {
        // Group items by vendor
        List<CartResponse.VendorGroup> vendorGroups = cart.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getVendor()))
                .entrySet().stream()
                .map(entry -> {
                    VendorProfile vendor = entry.getKey();
                    List<CartItem> items = entry.getValue();
                    
                    // Calculate subtotal for this vendor
                    BigDecimal subtotal = items.stream()
                            .map(item -> {
                                BigDecimal itemPrice = item.getProduct().getPrice();
                                if (item.getProductVariation() != null && item.getProductVariation().getPriceAdjustment() != null) {
                                    itemPrice = itemPrice.add(item.getProductVariation().getPriceAdjustment());
                                }
                                return itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    return CartResponse.VendorGroup.builder()
                            .vendorId(vendor.getId())
                            .vendorName(vendor.getBusinessName())
                            .subtotal(subtotal)
                            .items(items.stream()
                                    .map(this::mapToCartItemDto)
                                    .collect(Collectors.toList()))
                            .build();
                })
                .collect(Collectors.toList());
        
        // Calculate total
        BigDecimal total = vendorGroups.stream()
                .map(CartResponse.VendorGroup::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .vendorGroups(vendorGroups)
                .totalItems(cart.getItems().size())
                .total(total)
                .build();
    }

    private CartResponse.CartItemDto mapToCartItemDto(CartItem item) {
        BigDecimal itemPrice = item.getProduct().getPrice();
        if (item.getProductVariation() != null && item.getProductVariation().getPriceAdjustment() != null) {
            itemPrice = itemPrice.add(item.getProductVariation().getPriceAdjustment());
        }
        
        return CartResponse.CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .variationId(item.getProductVariation() != null ? item.getProductVariation().getId() : null)
                .variationAttributes(item.getProductVariation() != null ? item.getProductVariation().getAttributes() : null)
                .quantity(item.getQuantity())
                .price(itemPrice)
                .subtotal(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())))
                .imageUrl(item.getProduct().getImageUrls() != null && !item.getProduct().getImageUrls().isEmpty() 
                        ? item.getProduct().getImageUrls().get(0) : null)
                .build();
    }
}
