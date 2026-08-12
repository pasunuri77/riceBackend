package com.rice.controller;

import com.rice.dto.auth.ProfileUpdateRequest;
import com.rice.dto.auth.UserResponse;
import com.rice.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
    public UserResponse updateMe(Authentication authentication,
                                 @Valid @RequestBody ProfileUpdateRequest request) {
        return authService.updateProfile(authentication.getName(), request);
    }

    @PostMapping("/me/avatar")
    public UserResponse uploadAvatar(Authentication authentication,
                                     @RequestParam("file") MultipartFile file) {
        return authService.uploadAvatar(authentication.getName(), file);
    }
}
