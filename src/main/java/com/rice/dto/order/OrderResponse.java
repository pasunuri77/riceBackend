package com.rice.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

// Keeps the original flattened fields for existing frontend components while
// also exposing the complete item list for multi-item order displays.
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
    private String notes;
    private String quantity;
    private BigDecimal amount;
    private String paymentStatus;
    private String deliveryStatus;
    private String date;
    private List<ItemResponse> items;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ItemResponse {
        private String name;
        private Integer weight;
        private Integer qty;
    }
}
