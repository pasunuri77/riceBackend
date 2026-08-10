package com.rice.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UpdateOrderDeliveryInfoRequest {
    private Instant estimatedDeliveryDate;
    private String deliveryRemarks;
}
