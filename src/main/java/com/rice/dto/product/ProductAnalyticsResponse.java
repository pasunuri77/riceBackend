package com.rice.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class ProductAnalyticsResponse {
    private String productId;
    private Map<String, Long> counts; // keys: view, add_to_cart, purchase, view_30d, ...
}
