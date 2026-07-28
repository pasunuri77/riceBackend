package com.rice.dto.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String fullName;
    private String mobile;
    private String altMobile;
    private String flat;
    private String building;
    private String street;
    private String area;
    private String landmark;
    private String village;
    private String city;
    private String district;
    private String state;
    private String country;
    private String pincode;
    private String type;
    private String instructions;

    // Explicit name: Jackson/Lombok would otherwise both strip the "is" prefix and
    // serialize this as "default", but the frontend expects the key "isDefault".
    @JsonProperty("isDefault")
    private boolean isDefault;
}
