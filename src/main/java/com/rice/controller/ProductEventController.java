package com.rice.controller;

import com.rice.dto.product.ProductAnalyticsResponse;
import com.rice.entity.enums.ProductEventType;
import com.rice.service.ProductAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProductEventController {

    private final ProductAnalyticsService analyticsService;

    @PostMapping("/api/products/{id}/events")
    public void logEvent(@PathVariable String id, @RequestBody Map<String, String> body) {
        String type = body.get("type");
        if (type == null) return;
        try {
            ProductEventType t = ProductEventType.valueOf(type.toUpperCase().replace('-', '_'));
            analyticsService.logEvent(id, t);
        } catch (IllegalArgumentException ex) {
            // ignore invalid types
        }
    }

    @GetMapping("/api/admin/products/{id}/analytics")
    public ProductAnalyticsResponse analytics(@PathVariable String id) {
        Map<String, Long> counts = analyticsService.aggregateCounts(id);
        return ProductAnalyticsResponse.builder().productId(id).counts(counts).build();
    }
}
