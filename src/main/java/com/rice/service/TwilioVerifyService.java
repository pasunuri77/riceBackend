package com.rice.service;

import com.rice.exception.ApiException;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import com.twilio.rest.verify.v2.service.VerificationCheckCreator;
import com.twilio.rest.verify.v2.service.VerificationCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TwilioVerifyService {

    private static final long MIN_REQUEST_GAP_MS = 60_000L;
    private static final long MAX_REQUESTS_WINDOW_MS = 10 * 60_000L;
    private static final int MAX_REQUESTS_PER_WINDOW = 3;

    private final String accountSid;
    private final String authToken;
    private final String verifyServiceSid;
    private final Map<String, Long> lastRequestAtByPhone = new ConcurrentHashMap<>();
    private final Map<String, Integer> requestCountByPhone = new ConcurrentHashMap<>();
    private final Map<String, Long> requestWindowStartedAtByPhone = new ConcurrentHashMap<>();
    private final Map<String, Boolean> verifiedPhones = new ConcurrentHashMap<>();

    public TwilioVerifyService() {
        this("", "", "");
    }

    public TwilioVerifyService(
            @Value("${twilio.account-sid:}") String accountSid,
            @Value("${twilio.auth-token:}") String authToken,
            @Value("${twilio.verify-service-sid:}") String verifyServiceSid
    ) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.verifyServiceSid = verifyServiceSid;

        if (!accountSid.isBlank() && !authToken.isBlank() && !verifyServiceSid.isBlank()) {
            Twilio.init(accountSid, authToken);
        }
    }

    public void sendVerification(String phone) {
        String normalizedPhone = normalizePhoneNumber(phone);
        validateConfiguration();
        enforceRateLimit(normalizedPhone);

        try {
            VerificationCreator creator = Verification.creator(verifyServiceSid, normalizedPhone, "sms");
            creator.create();
            verifiedPhones.remove(normalizedPhone);
        } catch (com.twilio.exception.ApiException ex) {
            log.warn("Twilio verification send failed for phone {}", maskPhone(normalizedPhone));
            throw ApiException.badRequest("Unable to send mobile OTP. Please try again later");
        }
    }

    public boolean verifyCode(String phone, String code) {
        String normalizedPhone = normalizePhoneNumber(phone);
        validateConfiguration();

        try {
            VerificationCheckCreator creator = new VerificationCheckCreator(verifyServiceSid)
                    .setTo(normalizedPhone)
                    .setCode(code);
            VerificationCheck check = creator.create();
            boolean approved = "approved".equalsIgnoreCase(check.getStatus());
            if (approved) {
                verifiedPhones.put(normalizedPhone, true);
            }
            return approved;
        } catch (com.twilio.exception.ApiException ex) {
            log.warn("Twilio verification check failed for phone {}", maskPhone(normalizedPhone));
            return false;
        }
    }

    public boolean isPhoneVerified(String phone) {
        String normalizedPhone = normalizePhoneNumber(phone);
        return Boolean.TRUE.equals(verifiedPhones.get(normalizedPhone));
    }

    public boolean isConfigured() {
        return !accountSid.isBlank() && !authToken.isBlank() && !verifyServiceSid.isBlank();
    }

    public String normalizePhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            throw ApiException.badRequest("Phone number is required");
        }

        String trimmed = phone.trim();
        String digits = trimmed.replaceAll("\\D", "");

        if (digits.length() == 10 && (digits.startsWith("6") || digits.startsWith("7") || digits.startsWith("8") || digits.startsWith("9"))) {
            return "+91" + digits;
        }

        if (digits.length() == 12 && digits.startsWith("91")) {
            return "+" + digits;
        }

        if (trimmed.startsWith("+") && digits.length() == 12) {
            return "+" + digits;
        }

        throw ApiException.badRequest("Enter a valid Indian mobile number");
    }

    private void validateConfiguration() {
        if (!isConfigured()) {
            throw ApiException.badRequest("Mobile OTP is not configured yet");
        }
    }

    private void enforceRateLimit(String phone) {
        long now = System.currentTimeMillis();
        Long lastRequestAt = lastRequestAtByPhone.get(phone);
        if (lastRequestAt != null && now - lastRequestAt < MIN_REQUEST_GAP_MS) {
            throw ApiException.badRequest("Please wait 60 seconds before requesting another OTP");
        }

        long windowStartedAt = requestWindowStartedAtByPhone.getOrDefault(phone, now);
        int requestCount = requestCountByPhone.getOrDefault(phone, 0);

        if (now - windowStartedAt > MAX_REQUESTS_WINDOW_MS) {
            requestWindowStartedAtByPhone.put(phone, now);
            requestCountByPhone.put(phone, 1);
        } else if (requestCount >= MAX_REQUESTS_PER_WINDOW) {
            throw ApiException.badRequest("Too many OTP requests. Please try again later");
        } else {
            requestCountByPhone.put(phone, requestCount + 1);
        }

        lastRequestAtByPhone.put(phone, now);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() <= 4) {
            return phone;
        }
        return phone.substring(0, 3) + "******" + phone.substring(phone.length() - 2);
    }
}
