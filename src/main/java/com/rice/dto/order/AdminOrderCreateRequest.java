package com.rice.dto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body for POST /api/admin/orders
 * Allows admin to book an order on behalf of a customer
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderCreateRequest {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Order type is required")
    private String orderType;  // "online" or "offline"
    
    private String address;  // required for online, ignored for offline
    
    @NotNull(message = "Payment method is required")
    private String paymentMethod;
    
    @NotEmpty(message = "Items are required")
    private List<OrderItemRequest> items;
    
    private String notes;
    
    private String couponCode;
    
    @Builder.Default
    private boolean markAsPaid = false;
}
