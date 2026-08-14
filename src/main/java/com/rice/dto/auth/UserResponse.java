package com.rice.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private boolean mobileVerified;
    private String role;
    private String image;
    private String photoUrl;
}
