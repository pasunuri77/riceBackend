package com.rice.dto.settings;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class StoreSettingsRequest {
    @NotBlank
    private String storeName;

    private String gstNumber;
    private String phone;

    @Email
    private String email;

    private String currency;

    @DecimalMin("0.0")
    private BigDecimal deliveryCharge;

    @DecimalMin("0.0")
    private BigDecimal freeDeliveryThreshold;

    @DecimalMin("0.0")
    private BigDecimal taxPercentage;

    private Map<String, BusinessHourRequest> businessHours;

    @Getter
    @Setter
    public static class BusinessHourRequest {
        private String open;
        private String close;
        private Boolean closed;
    }
}
