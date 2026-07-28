package com.rice.entity.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AddressType {
    @JsonProperty("Home") HOME,
    @JsonProperty("Office") OFFICE,
    @JsonProperty("Shop") SHOP,
    @JsonProperty("Warehouse") WAREHOUSE,
    @JsonProperty("Other") OTHER
}
