package com.rice.entity.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PaymentStatus {
    @JsonProperty("Pending") PENDING,
    @JsonProperty("Paid") PAID
}
