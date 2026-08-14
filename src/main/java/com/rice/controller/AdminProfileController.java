package com.rice.controller;

import com.rice.dto.auth.ProfileUpdateRequest;
import com.rice.dto.auth.UserResponse;
import com.rice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse uploadPhoto(Authentication authentication,
                                   @RequestParam("file") MultipartFile file) {
        return authService.uploadPhoto(authentication.getName(), file);
    }

    @DeleteMapping("/photo")
    public UserResponse deletePhoto(Authentication authentication) {
        return authService.deletePhoto(authentication.getName());
    }
}
