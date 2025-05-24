package com.martello.ecommerce.service;

import com.martello.ecommerce.exception.BadRequestException;
import com.martello.ecommerce.exception.ResourceNotFoundException;
import com.martello.ecommerce.model.dto.ProductRequest;
import com.martello.ecommerce.model.dto.ProductResponse;
import com.martello.ecommerce.model.entity.Category;
import com.martello.ecommerce.model.entity.Product;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.model.entity.VendorProfile;
import com.martello.ecommerce.model.enums.ApprovalStatus;
import com.martello.ecommerce.repository.CategoryRepository;
import com.martello.ecommerce.repository.ProductRepository;
import com.martello.ecommerce.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final VendorProfileRepository vendorProfileRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsListedTrue(pageable);
        return products.map(this::mapToProductResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        if (!product.getIsListed()) {
            throw new ResourceNotFoundException("Product not found");
        }
        
        return mapToProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request, User user) {
        VendorProfile vendorProfile = vendorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
        
        // Check if vendor is approved
        if (vendorProfile.getStatus() != ApprovalStatus.APPROVED) {
            throw new BadRequestException("Vendor is not approved to add products");
        }
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(category)
                .vendor(vendorProfile)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isListed(true) // Auto-approve for approved vendors
                .imageUrls(request.getImageUrls())
                .build();
        
        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, User user) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Verify ownership
        if (!product.getVendor().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to update this product");
        }
        
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }
        
        // Update fields
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (category != null) {
            product.setCategory(category);
        }
        if (request.getImageUrls() != null) {
            product.setImageUrls(request.getImageUrls());
        }
        
        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id, User user) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Verify ownership
        if (!product.getVendor().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to delete this product");
        }
        
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getVendorProducts(User user) {
        VendorProfile vendorProfile = vendorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
        
        List<Product> products = productRepository.findByVendorId(vendorProfile.getId());
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    public List<ProductResponse> getFeaturedProducts() {
        // Example: returns top 10 listed products with most orderItems
        Pageable topTen = PageRequest.of(0, 10);
        List<Product> topProducts = productRepository.findTopProductsByOrderCount(topTen);
        return topProducts.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    public List<ProductResponse> getRelatedProducts(Long productId, Long categoryId) {
        List<Product> related = productRepository.findTop5ByCategoryIdAndIdNotAndIsListedTrue(categoryId, productId);
        return related.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .vendorId(product.getVendor().getId())
                .vendorName(product.getVendor().getBusinessName())
                .isListed(product.getIsListed())
                .imageUrls(product.getImageUrls())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
