package com.rice.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MobileOtpRequest {

    @NotBlank
    @Pattern(regexp = "^(\\+91)?[6-9]\\d{9}$", message = "Enter a valid Indian mobile number")
    private String phone;
}
