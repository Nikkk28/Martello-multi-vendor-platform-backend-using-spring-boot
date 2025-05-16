package com.martello.ecommerce.controller;

import com.martello.ecommerce.model.dto.ApiResponse;
import com.martello.ecommerce.model.dto.ReviewRequest;
import com.martello.ecommerce.model.dto.ReviewResponse;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId,
            Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/products/{productId}/rating")
    public ResponseEntity<ApiResponse<Double>> getProductAverageRating(@PathVariable Long productId) {
        Double rating = reviewService.getProductAverageRating(productId);
        return ResponseEntity.ok(ApiResponse.success(rating));
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal User user) {
        ReviewResponse review = reviewService.createReview(productId, request, user);
        return ResponseEntity.ok(ApiResponse.success(review, "Review submitted successfully and awaiting approval"));
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal User user) {
        ReviewResponse review = reviewService.updateReview(reviewId, request, user);
        return ResponseEntity.ok(ApiResponse.success(review, "Review updated successfully and awaiting approval"));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user) {
        reviewService.deleteReview(reviewId, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
    }

    @PatchMapping("/admin/reviews/{reviewId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReviewResponse>> approveReview(@PathVariable Long reviewId) {
        ReviewResponse review = reviewService.approveReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success(review, "Review approved successfully"));
    }

    @GetMapping("/admin/reviews/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getPendingReviews(Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getPendingReviews(pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
}
