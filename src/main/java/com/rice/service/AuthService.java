package com.rice.service;

import com.rice.dto.auth.AuthResponse;
import com.rice.dto.auth.EmailOtpRequest;
import com.rice.dto.auth.LoginRequest;
import com.rice.dto.auth.RegisterRequest;
import com.rice.dto.auth.ResetPasswordRequest;
import com.rice.dto.auth.UserResponse;
import com.rice.dto.auth.VerifyOtpRequest;
import com.rice.entity.User;
import com.rice.entity.enums.OtpPurpose;
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
    private final EmailOtpService emailOtpService;

    public AuthResponse login(LoginRequest request) {
        String email = emailOtpService.normalize(request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

        String token = jwtService.generateToken(new AppUserPrincipal(user));
        return AuthResponse.builder().token(token).user(toResponse(user)).build();
    }

    public void sendRegistrationOtp(EmailOtpRequest request) {
        String email = emailOtpService.normalize(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("An account with this email already exists");
        }
        emailOtpService.sendOtp(email, OtpPurpose.REGISTRATION);
    }

    public void verifyRegistrationOtp(VerifyOtpRequest request) {
        String email = emailOtpService.normalize(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("An account with this email already exists");
        }
        emailOtpService.verifyOtp(email, request.getOtp(), OtpPurpose.REGISTRATION);
    }

    public AuthResponse register(RegisterRequest request) {
        String email = emailOtpService.normalize(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("An account with this email already exists");
        }
        emailOtpService.consumeVerifiedOtp(email, OtpPurpose.REGISTRATION);

        User user = User.builder()
                .name(request.getFullName())
                .email(email)
                .phone(request.getMobile())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);

        String token = jwtService.generateToken(new AppUserPrincipal(user));
        return AuthResponse.builder().token(token).user(toResponse(user)).build();
    }

    public void sendPasswordResetOtp(EmailOtpRequest request) {
        String email = emailOtpService.normalize(request.getEmail());
        userRepository.findByEmail(email)
                .ifPresent(user -> emailOtpService.sendOtp(email, OtpPurpose.PASSWORD_RESET));
    }

    public void resetPassword(ResetPasswordRequest request) {
        String email = emailOtpService.normalize(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("User not found"));

        emailOtpService.consumeOtp(email, request.getOtp(), OtpPurpose.PASSWORD_RESET);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
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
