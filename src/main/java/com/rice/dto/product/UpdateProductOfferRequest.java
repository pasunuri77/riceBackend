package com.rice.dto.product;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UpdateProductOfferRequest {
    private Boolean showInTodaysOffers;
    private Integer displayPriority;
    private Instant offerEndDate;
    private Integer lowStockThreshold;
}
