package com.rice.service;

import com.rice.dto.auth.AuthResponse;
import com.rice.dto.auth.LoginRequest;
import com.rice.dto.auth.RegisterRequest;
import com.rice.dto.auth.UserResponse;
import com.rice.entity.User;
import com.rice.entity.enums.Role;
import com.rice.exception.ApiException;
import com.rice.repository.UserRepository;
import com.rice.security.AppUserPrincipal;
import com.rice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

        String token = jwtService.generateToken(new AppUserPrincipal(user));
        return AuthResponse.builder().token(token).user(toResponse(user)).build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.getFullName())
                .email(request.getEmail())
                .phone(request.getMobile())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);

        String token = jwtService.generateToken(new AppUserPrincipal(user));
        return AuthResponse.builder().token(token).user(toResponse(user)).build();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name().toLowerCase())
                .build();
    }
}
