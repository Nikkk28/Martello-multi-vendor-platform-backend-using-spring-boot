package com.martello.ecommerce.service;

import com.martello.ecommerce.exception.BadRequestException;
import com.martello.ecommerce.exception.ResourceNotFoundException;
import com.martello.ecommerce.model.dto.ReviewRequest;
import com.martello.ecommerce.model.dto.ReviewResponse;
import com.martello.ecommerce.model.entity.Order;
import com.martello.ecommerce.model.entity.Product;
import com.martello.ecommerce.model.entity.Review;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.model.enums.Role;
import com.martello.ecommerce.repository.OrderRepository;
import com.martello.ecommerce.repository.ProductRepository;
import com.martello.ecommerce.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductIdAndIsApprovedTrue(productId, pageable);
        return reviews.map(this::mapToReviewResponse);
    }

    @Transactional(readOnly = true)
    public Double getProductAverageRating(Long productId) {
        Double averageRating = reviewRepository.getAverageRatingForProduct(productId);
        return averageRating != null ? averageRating : 0.0;
    }

    @Transactional
    public ReviewResponse createReview(Long productId, ReviewRequest request, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Check if user has already reviewed this product
        if (reviewRepository.findByProductIdAndUserId(productId, user.getId()).isPresent()) {
            throw new BadRequestException("You have already reviewed this product");
        }
        
        // Check if user has purchased this product
        List<Order> userOrders = orderRepository.findByCustomerId(user.getId());
        boolean hasPurchased = userOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(productId));
        
        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .isVerifiedPurchase(hasPurchased)
                .isApproved(false) // Reviews need approval by default
                .build();
        
        Review savedReview = reviewRepository.save(review);
        
        // Notify vendor about new review
        notificationService.sendNotification(
                product.getVendor().getUser(),
                "New Product Review",
                "Your product '" + product.getName() + "' has received a new review"
        );
        
        return mapToReviewResponse(savedReview);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        // Verify ownership
        if (!review.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to update this review");
        }
        
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setIsApproved(false); // Reset approval status on update
        
        Review updatedReview = reviewRepository.save(review);
        return mapToReviewResponse(updatedReview);
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        // Verify ownership or admin
        if (!review.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new BadRequestException("You don't have permission to delete this review");
        }
        
        reviewRepository.delete(review);
    }

    @Transactional
    public ReviewResponse approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        review.setIsApproved(true);
        Review approvedReview = reviewRepository.save(review);
        
        // Notify user that their review was approved
        notificationService.sendNotification(
                review.getUser(),
                "Review Approved",
                "Your review for '" + review.getProduct().getName() + "' has been approved"
        );
        
        return mapToReviewResponse(approvedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getPendingReviews(Pageable pageable) {
        Page<Review> pendingReviews = reviewRepository.findByIsApprovedFalse(pageable);
        return pendingReviews.map(this::mapToReviewResponse);
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .isApproved(review.getIsApproved())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
