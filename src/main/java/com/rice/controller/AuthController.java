package com.rice.controller;

import com.rice.dto.auth.AuthResponse;
import com.rice.dto.auth.EmailOtpRequest;
import com.rice.dto.auth.LoginRequest;
import com.rice.dto.auth.MessageResponse;
import com.rice.dto.auth.RegisterRequest;
import com.rice.dto.auth.ResetPasswordRequest;
import com.rice.dto.auth.VerifyOtpRequest;
import com.rice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/send-otp")
    public MessageResponse sendRegistrationOtp(@Valid @RequestBody EmailOtpRequest request) {
        authService.sendRegistrationOtp(request);
        return new MessageResponse("OTP sent to your email");
    }

    @PostMapping("/verify-otp")
    public MessageResponse verifyRegistrationOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyRegistrationOtp(request);
        return new MessageResponse("Email OTP verified");
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody EmailOtpRequest request) {
        authService.sendPasswordResetOtp(request);
        return new MessageResponse("If the email exists, a password reset OTP has been sent");
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return new MessageResponse("Password reset successfully");
    }

    @PostMapping("/logout")
    public void logout() {
        // JWTs are stateless - logout is handled client-side by discarding the token.
        // Kept as an endpoint so the frontend's authApi.logout() call has somewhere to go.
    }
}
