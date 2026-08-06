package com.rice.dto.coupon;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CouponValidateRequest {
    @NotBlank
    private String code;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal subtotal;
}
