package com.rice.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ServiceabilityResponse {
    private boolean serviceable;
    private String pincode;
}
