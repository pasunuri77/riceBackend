package com.rice.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AdminPincodesRequest {
    private List<String> pincodes;
}
