package com.rice.entity.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PaymentMethod {
    @JsonProperty("upi") UPI,
    @JsonProperty("card") CARD,
    @JsonProperty("netbanking") NETBANKING,
    @JsonProperty("cod") COD
}
