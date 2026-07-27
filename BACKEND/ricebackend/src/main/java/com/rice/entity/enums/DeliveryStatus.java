package com.rice.entity.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DeliveryStatus {
    @JsonProperty("Pending") PENDING,
    @JsonProperty("Processing") PROCESSING,
    @JsonProperty("Shipped") SHIPPED,
    @JsonProperty("Delivered") DELIVERED,
    @JsonProperty("Cancelled") CANCELLED
}
