package com.rice.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPincodesRequest {
    private List<String> pincodes;
    private String city;
    private List<PincodeDto> items;

    public AdminPincodesRequest(List<String> pincodes) {
        this.pincodes = pincodes;
    }
}
