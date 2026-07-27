package com.rice.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

// Flattened shape matching the existing frontend Order display components
// (Admin Orders table, User Orders list, Customer detail modal all expect a single
// riceName/quantity/image per order, not a nested item list).
@Getter
@Builder
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String customerId;
    private String customerName;
    private String productId;
    private String riceName;
    private String image;
    private String address;
    private String quantity;
    private BigDecimal amount;
    private String paymentStatus;
    private String deliveryStatus;
    private String date;
}
