package com.rice.service;

import com.rice.dto.ReviewResponse;
import com.rice.repository.ReviewRepository;
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
}
