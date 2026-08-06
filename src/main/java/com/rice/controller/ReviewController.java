package com.rice.controller;

import com.rice.dto.ReviewRequest;
import com.rice.dto.ReviewResponse;
import com.rice.security.AppUserPrincipal;
import com.rice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public List<ReviewResponse> list(@PathVariable String productId) {
        return reviewService.listByProduct(productId);
    }

    @PostMapping
    public ReviewResponse create(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable String productId,
            @Valid @RequestBody ReviewRequest request
    ) {
        return reviewService.create(productId, principal.getUser(), request);
    }
}
