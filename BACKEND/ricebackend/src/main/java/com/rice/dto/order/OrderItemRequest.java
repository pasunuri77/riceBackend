package com.rice.dto.order;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// Mirrors a cart item from the frontend's CartContext: { id, name, brand, image, pricePerKg, weight, qty }
@Getter
@Setter
public class OrderItemRequest {
    private String id; // productId
    private String name;
    private String image;
    private BigDecimal pricePerKg;
    private Integer weight;
    private Integer qty;
}
