package com.martello.ecommerce.controller;

import com.martello.ecommerce.model.dto.ApiResponse;
import com.martello.ecommerce.model.dto.ProductRequest;
import com.martello.ecommerce.model.dto.ProductResponse;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(Pageable pageable) {
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
//        ProductResponse product = productService.getProductById(id);
//        return ResponseEntity.ok(ApiResponse.success(product));
//    }
    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts() {
        List<ProductResponse> featuredProducts = productService.getFeaturedProducts();
        return ResponseEntity.ok(featuredProducts);
    }
    @GetMapping("/related")
    public ResponseEntity<List<ProductResponse>> getRelatedProducts(
            @RequestParam Long productId,
            @RequestParam Long categoryId) {
        List<ProductResponse> relatedProducts = productService.getRelatedProducts(productId, categoryId);
        return ResponseEntity.ok(relatedProducts);
    }
    @GetMapping("/id")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@RequestParam("id") Long productId) {
        ProductResponse product = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

}
