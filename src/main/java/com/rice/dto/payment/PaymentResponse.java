package com.rice.dto.payment;

import com.rice.entity.enums.PaymentMethod;
import com.rice.entity.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private String orderType;
    private Long orderId;
    private String displayOrderId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private Instant paidAt;
    private Instant createdAt;
}
