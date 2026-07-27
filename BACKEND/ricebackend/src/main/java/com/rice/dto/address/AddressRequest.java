package com.rice.dto.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {
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

    @JsonProperty("isDefault")
    private boolean isDefault;
}
