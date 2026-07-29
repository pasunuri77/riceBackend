package com.rice.controller;

import com.rice.dto.auth.ProfileUpdateRequest;
import com.rice.dto.auth.UserResponse;
import com.rice.security.AppUserPrincipal;
import com.rice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @PatchMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal AppUserPrincipal principal,
                                 @Valid @RequestBody ProfileUpdateRequest request) {
        return authService.updateProfile(principal.getUser(), request);
    }
}
