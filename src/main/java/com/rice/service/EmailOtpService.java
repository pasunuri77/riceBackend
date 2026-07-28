package com.rice.service;

import com.rice.email.EmailService;
import com.rice.entity.OtpVerification;
import com.rice.entity.enums.OtpPurpose;
import com.rice.exception.ApiException;
import com.rice.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailOtpService {

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendOtp(String email, OtpPurpose purpose) {
        String normalizedEmail = normalize(email);
        String otp = generateOtp();

        otpRepository.deleteByEmailAndPurpose(normalizedEmail, purpose);
        otpRepository.save(OtpVerification.builder()
                .email(normalizedEmail)
                .otpHash(passwordEncoder.encode(otp))
                .purpose(purpose)
                .expiresAt(Instant.now().plus(OTP_TTL))
                .build());

        if (purpose == OtpPurpose.PASSWORD_RESET) {
            emailService.sendPasswordResetOtp(normalizedEmail, otp);
            return;
        }

        emailService.sendPin(normalizedEmail, otp);
    }

    @Transactional
    public void verifyOtp(String email, String otp, OtpPurpose purpose) {
        OtpVerification verification = getLatestOtp(email, purpose);
        validateOtp(verification, otp);
        verification.setVerifiedAt(Instant.now());
    }

    @Transactional
    public void consumeVerifiedOtp(String email, OtpPurpose purpose) {
        String normalizedEmail = normalize(email);
        OtpVerification verification = getLatestOtp(normalizedEmail, purpose);

        if (verification.getVerifiedAt() == null) {
            throw ApiException.badRequest("Please verify the email OTP first");
        }
        if (verification.getExpiresAt().isBefore(Instant.now())) {
            otpRepository.deleteByEmailAndPurpose(normalizedEmail, purpose);
            throw ApiException.badRequest("OTP has expired. Please request a new code");
        }

        otpRepository.deleteByEmailAndPurpose(normalizedEmail, purpose);
    }

    @Transactional
    public void consumeOtp(String email, String otp, OtpPurpose purpose) {
        String normalizedEmail = normalize(email);
        OtpVerification verification = getLatestOtp(normalizedEmail, purpose);
        validateOtp(verification, otp);
        otpRepository.deleteByEmailAndPurpose(normalizedEmail, purpose);
    }

    public String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private OtpVerification getLatestOtp(String email, OtpPurpose purpose) {
        return otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(normalize(email), purpose)
                .orElseThrow(() -> ApiException.badRequest("OTP not found. Please request a new code"));
    }

    private void validateOtp(OtpVerification verification, String otp) {
        if (verification.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.badRequest("OTP has expired. Please request a new code");
        }
        if (!passwordEncoder.matches(otp, verification.getOtpHash())) {
            throw ApiException.badRequest("Invalid OTP");
        }
    }

    private String generateOtp() {
        return String.valueOf(100000 + RANDOM.nextInt(900000));
    }
}
