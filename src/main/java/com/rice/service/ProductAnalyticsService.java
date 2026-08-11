package com.rice.service;

import com.rice.entity.ProductEvent;
import com.rice.entity.enums.ProductEventType;
import com.rice.repository.ProductEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductAnalyticsService {

    private final ProductEventRepository repo;

    @Transactional
    public void logEvent(String productId, ProductEventType type) {
        ProductEvent e = ProductEvent.builder()
                .productId(productId)
                .type(type)
                .createdAt(Instant.now())
                .build();
        repo.save(e);
    }

    public Map<String, Long> aggregateCounts(String productId) {
        Map<String, Long> out = new HashMap<>();
        for (ProductEventType t : ProductEventType.values()) {
            long c = repo.countByProductIdAndType(productId, t);
            out.put(t.name().toLowerCase(), c);
        }
        // additional recent 30-day counts
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        for (ProductEventType t : ProductEventType.values()) {
            long c = repo.countByProductAndTypeSince(productId, t, since);
            out.put(t.name().toLowerCase() + "_30d", c);
        }
        return out;
    }
}
