package com.rice.entity.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserStatus {
    @JsonProperty("Active") ACTIVE,
    @JsonProperty("Blocked") BLOCKED
}
