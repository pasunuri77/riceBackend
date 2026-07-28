package com.rice.controller;

import com.rice.dto.auth.AuthResponse;
import com.rice.dto.auth.LoginRequest;
import com.rice.dto.auth.RegisterRequest;
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

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/logout")
    public void logout() {
        // JWTs are stateless - logout is handled client-side by discarding the token.
        // Kept as an endpoint so the frontend's authApi.logout() call has somewhere to go.
    }
}
