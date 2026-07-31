package com.rice.dto.settings;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
public class StoreSettingsResponse {
    private String storeName;
    private String gstNumber;
    private String phone;
    private String email;
    private String currency;
    private BigDecimal deliveryCharge;
    private BigDecimal freeDeliveryThreshold;
    private BigDecimal taxPercentage;
    private Map<String, StoreSettingsRequest.BusinessHourRequest> businessHours;
}
