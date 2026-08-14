package com.rice.service;

import com.rice.dto.auth.AuthResponse;
import com.rice.dto.auth.EmailOtpRequest;
import com.rice.dto.auth.LoginRequest;
import com.rice.dto.auth.MessageResponse;
import com.rice.dto.auth.MobileOtpRequest;
import com.rice.dto.auth.ProfileUpdateRequest;
import com.rice.dto.auth.RegisterRequest;
import com.rice.dto.auth.ResetPasswordRequest;
import com.rice.dto.auth.UserResponse;
import com.rice.dto.auth.VerifyMobileOtpRequest;
import com.rice.dto.auth.VerifyOtpRequest;
import com.rice.entity.User;
import com.rice.entity.enums.OtpPurpose;
import com.rice.entity.enums.Role;
import com.rice.exception.ApiException;
import com.rice.repository.UserRepository;
import com.rice.security.AppUserPrincipal;
import com.rice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailOtpService emailOtpService;
    private final TwilioVerifyService twilioVerifyService;
    private final CloudinaryService cloudinaryService;

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

    public MessageResponse sendMobileOtp(MobileOtpRequest request) {
        String phone = twilioVerifyService.normalizePhoneNumber(request.getPhone());
        if (userRepository.existsByPhone(phone)) {
            throw ApiException.conflict("This mobile number is already registered");
        }
        twilioVerifyService.sendVerification(phone);
        return new MessageResponse("OTP sent successfully");
    }

    public MessageResponse verifyMobileOtp(VerifyMobileOtpRequest request, Authentication authentication) {
        String phone = twilioVerifyService.normalizePhoneNumber(request.getPhone());
        if (userRepository.existsByPhone(phone) && authentication == null) {
            throw ApiException.conflict("This mobile number is already registered");
        }

        boolean approved = twilioVerifyService.verifyCode(phone, request.getOtp());
        if (!approved) {
            throw ApiException.badRequest("Invalid or expired OTP");
        }

        if (authentication != null) {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> ApiException.unauthorized("Please login again"));
            user.setPhone(phone);
            user.setMobileVerified(true);
            userRepository.save(user);
        }

        return new MessageResponse("Mobile number verified successfully");
    }

    public AuthResponse register(RegisterRequest request) {
        String email = emailOtpService.normalize(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("An account with this email already exists");
        }
        emailOtpService.consumeVerifiedOtp(email, OtpPurpose.REGISTRATION);

        String phone = twilioVerifyService.normalizePhoneNumber(request.getMobile());
        if (twilioVerifyService.isConfigured() && !twilioVerifyService.isPhoneVerified(phone)) {
            throw ApiException.badRequest("Please verify your mobile number before completing registration");
        }

        User user = User.builder()
                .name(request.getFullName())
                .email(email)
                .phone(phone)
                .mobileVerified(twilioVerifyService.isConfigured() && twilioVerifyService.isPhoneVerified(phone))
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

    @Transactional
    public UserResponse updateProfile(User principalUser, ProfileUpdateRequest request) {
        User user = userRepository.findById(principalUser.getId())
                .orElseThrow(() -> ApiException.notFound("User not found"));
        return updateProfileUser(user, request);
    }

    @Transactional
    public UserResponse updateProfile(String authenticatedEmail, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> ApiException.unauthorized("Please login again"));
        return updateProfileUser(user, request);
    }

    private UserResponse updateProfileUser(User user, ProfileUpdateRequest request) {
        String email = emailOtpService.normalize(request.getEmail());

        if (userRepository.existsByEmailAndIdNot(email, user.getId())) {
            throw ApiException.conflict("An account with this email already exists");
        }

        String phone = twilioVerifyService.normalizePhoneNumber(request.getMobile());
        user.setName(request.getFullName());
        user.setEmail(email);
        if (!phone.equals(user.getPhone())) {
            user.setMobileVerified(false);
        }
        user.setPhone(phone);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse uploadAvatar(String authenticatedEmail, org.springframework.web.multipart.MultipartFile file) {
        return uploadPhoto(authenticatedEmail, file);
    }

    @Transactional
    public UserResponse uploadPhoto(String authenticatedEmail, org.springframework.web.multipart.MultipartFile file) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> ApiException.unauthorized("Please login again"));
        String url = cloudinaryService.upload(file);
        user.setImage(url);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse deletePhoto(String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> ApiException.unauthorized("Please login again"));
        user.setImage(null);
        return toResponse(userRepository.save(user));
    }

    public UserResponse toResponse(User user) {
        String photoUrl = user.getImage();
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .mobileVerified(user.isMobileVerified())
                .role(user.getRole().name().toLowerCase())
                .image(photoUrl)
                .photoUrl(photoUrl)
                .build();
    }
}
