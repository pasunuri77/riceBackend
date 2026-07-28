package com.rice.entity.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ProductStatus {
    @JsonProperty("Active") ACTIVE,
    @JsonProperty("Inactive") INACTIVE
}
