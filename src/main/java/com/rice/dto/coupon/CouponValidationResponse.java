package com.rice.dto.coupon;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CouponValidationResponse {
    private boolean valid;
    private String message;
    private CouponResponse coupon;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;
}
