package com.rice.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StoreLocationResponse {
    private String name;
    private String address;
    private String area;
    private String city;
    private String state;
    private String stateCode;
    private String zip;
    private String country;
    private String countryCode;
}
