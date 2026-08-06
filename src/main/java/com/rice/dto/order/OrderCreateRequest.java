package com.rice.dto.order;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderCreateRequest {
    private String address;
    private String paymentMethod;
    private String couponCode;
    private String notes;

    @NotEmpty
    private List<OrderItemRequest> items;
    // amount is intentionally NOT accepted from the client - the server recomputes
    // it from items to avoid trusting a client-supplied total.
}
