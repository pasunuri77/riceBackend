package com.rice.controller;

import com.rice.dto.auth.ProfileUpdateRequest;
import com.rice.dto.auth.UserResponse;
import com.rice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final AuthService authService;

    @PatchMapping
    public UserResponse updateProfile(Authentication authentication,
                                      @Valid @RequestBody ProfileUpdateRequest request) {
        return authService.updateProfile(authentication.getName(), request);
    }
}
