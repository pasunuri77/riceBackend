package com.rice.service;

import com.rice.dto.ReviewRequest;
import com.rice.dto.ReviewResponse;
import com.rice.entity.Review;
import com.rice.entity.User;
import com.rice.exception.ApiException;
import com.rice.repository.OrderRepository;
import com.rice.repository.ReviewRepository;
import com.rice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public List<ReviewResponse> listByProduct(String productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(r -> ReviewResponse.builder()
                        .id(r.getId())
                        .productId(r.getProduct().getId())
                        .name(r.getReviewerName())
                        .rating(r.getRating())
                        .date(DATE_FORMAT.format(r.getCreatedAt().atZone(ZoneOffset.UTC)))
                        .comment(r.getComment())
                        .build())
                .toList();
    }

    @Transactional
    public ReviewResponse create(String productId, User customer, ReviewRequest request) {
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw ApiException.badRequest("Rating must be between 1 and 5");
        }
        if (!orderRepository.existsByCustomerIdAndItemsProductId(customer.getId(), productId)) {
            throw ApiException.forbidden("You can only review products you have purchased");
        }
        var product = productRepository.findById(productId)
                .orElseThrow(() -> ApiException.notFound("Product not found: " + productId));
        Review review = Review.builder()
                .product(product)
                .user(customer)
                .reviewerName(customer.getName())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        Review saved = reviewRepository.save(review);
        return ReviewResponse.builder()
                .id(saved.getId())
                .productId(productId)
                .name(saved.getReviewerName())
                .rating(saved.getRating())
                .date(DATE_FORMAT.format(saved.getCreatedAt().atZone(ZoneOffset.UTC)))
                .comment(saved.getComment())
                .build();
    }
}
