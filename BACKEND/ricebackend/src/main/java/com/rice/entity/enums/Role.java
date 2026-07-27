package com.rice.entity.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

// JSON values kept lowercase to match the frontend's user.role checks ('admin' / 'user')
public enum Role {
    @JsonProperty("user") USER,
    @JsonProperty("admin") ADMIN
}
