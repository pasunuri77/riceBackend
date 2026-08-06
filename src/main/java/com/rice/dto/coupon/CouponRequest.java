package com.rice.dto.coupon;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class CouponRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String type;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal value;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal minOrder;

    @NotNull
    private Instant expiresAt;

    @NotNull
    private Boolean active;
}
