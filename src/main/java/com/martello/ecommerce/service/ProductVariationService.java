package com.martello.ecommerce.service;

import com.martello.ecommerce.exception.BadRequestException;
import com.martello.ecommerce.exception.ResourceNotFoundException;
import com.martello.ecommerce.model.dto.ProductVariationRequest;
import com.martello.ecommerce.model.dto.ProductVariationResponse;
import com.martello.ecommerce.model.entity.Product;
import com.martello.ecommerce.model.entity.ProductVariation;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.repository.ProductRepository;
import com.martello.ecommerce.repository.ProductVariationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariationService {

    private final ProductVariationRepository productVariationRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductVariationResponse> getProductVariations(Long productId) {
        List<ProductVariation> variations = productVariationRepository.findByProductIdAndIsActiveTrue(productId);
        return variations.stream()
                .map(this::mapToProductVariationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductVariationResponse getProductVariation(Long productId, Long variationId) {
        ProductVariation variation = productVariationRepository.findById(variationId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variation not found"));
        
        if (!variation.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Product variation does not belong to the specified product");
        }
        
        return mapToProductVariationResponse(variation);
    }

    @Transactional
    public ProductVariationResponse createProductVariation(Long productId, ProductVariationRequest request, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Verify ownership
        if (!product.getVendor().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to add variations to this product");
        }
        
        // Check if SKU already exists for this product
        if (productVariationRepository.findByProductIdAndSku(productId, request.getSku()).isPresent()) {
            throw new BadRequestException("A variation with this SKU already exists for this product");
        }
        
        ProductVariation variation = ProductVariation.builder()
                .product(product)
                .sku(request.getSku())
                .attributes(request.getAttributes())
                .stockQuantity(request.getStockQuantity())
                .priceAdjustment(request.getPriceAdjustment())
                .isActive(request.getIsActive())
                .build();
        
        ProductVariation savedVariation = productVariationRepository.save(variation);
        return mapToProductVariationResponse(savedVariation);
    }

    @Transactional
    public ProductVariationResponse updateProductVariation(Long productId, Long variationId, 
                                                          ProductVariationRequest request, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Verify ownership
        if (!product.getVendor().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to update variations for this product");
        }
        
        ProductVariation variation = productVariationRepository.findById(variationId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variation not found"));
        
        if (!variation.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Product variation does not belong to the specified product");
        }
        
        // Check if SKU is being changed and if it already exists
        if (!variation.getSku().equals(request.getSku()) && 
            productVariationRepository.findByProductIdAndSku(productId, request.getSku()).isPresent()) {
            throw new BadRequestException("A variation with this SKU already exists for this product");
        }
        
        variation.setSku(request.getSku());
        variation.setAttributes(request.getAttributes());
        variation.setStockQuantity(request.getStockQuantity());
        variation.setPriceAdjustment(request.getPriceAdjustment());
        variation.setIsActive(request.getIsActive());
        
        ProductVariation updatedVariation = productVariationRepository.save(variation);
        return mapToProductVariationResponse(updatedVariation);
    }

    @Transactional
    public void deleteProductVariation(Long productId, Long variationId, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Verify ownership
        if (!product.getVendor().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to delete variations for this product");
        }
        
        ProductVariation variation = productVariationRepository.findById(variationId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variation not found"));
        
        if (!variation.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Product variation does not belong to the specified product");
        }
        
        productVariationRepository.delete(variation);
    }

    private ProductVariationResponse mapToProductVariationResponse(ProductVariation variation) {
        return ProductVariationResponse.builder()
                .id(variation.getId())
                .productId(variation.getProduct().getId())
                .sku(variation.getSku())
                .attributes(variation.getAttributes())
                .stockQuantity(variation.getStockQuantity())
                .priceAdjustment(variation.getPriceAdjustment())
                .isActive(variation.getIsActive())
                .createdAt(variation.getCreatedAt())
                .updatedAt(variation.getUpdatedAt())
                .build();
    }
}
