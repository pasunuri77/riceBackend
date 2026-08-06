package com.rice.dto.coupon;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class CouponResponse {
    private Long id;
    private String code;
    private String type;
    private BigDecimal value;
    private BigDecimal minOrder;
    private Instant expiresAt;
    private Boolean active;
}
