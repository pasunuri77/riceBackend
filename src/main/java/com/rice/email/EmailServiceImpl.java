package com.rice.email;

import com.rice.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final String BREVO_SEND_EMAIL_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${app.email.sender-name:Rice Store}")
    private String senderName;

    @Value("${app.email.sender-email:pasunurisagar2001@gmail.com}")
    private String senderEmail;

    @Override
    public void sendPin(String email, String pin) {
        String html = """
                <div style="background:#f6f7f2;padding:32px 16px;font-family:Segoe UI,Arial,sans-serif;">
                    <div style="max-width:560px;margin:auto;background:white;border-radius:14px;overflow:hidden;border:1px solid #e5e7eb;">
                        <div style="background:#166534;color:white;padding:28px;text-align:center;">
                            <h1 style="margin:0;font-size:24px;">Rice Store</h1>
                            <p style="margin:8px 0 0;color:#dcfce7;">Email verification</p>
                        </div>
                        <div style="padding:32px;">
                            <h2 style="margin-top:0;color:#111827;">Verify your email</h2>
                            <p style="color:#4b5563;line-height:24px;">Use this 6-digit OTP to complete your account verification.</p>
                            <div style="text-align:center;margin:28px 0;">
                                <span style="display:inline-block;font-size:42px;font-weight:700;letter-spacing:8px;color:#166534;background:#f0fdf4;border:1px solid #bbf7d0;border-radius:12px;padding:18px 28px;">%s</span>
                            </div>
                            <p style="color:#b91c1c;font-weight:600;">This OTP expires in 5 minutes.</p>
                            <p style="color:#6b7280;font-size:13px;">If you did not request this email, you can ignore it.</p>
                        </div>
                    </div>
                </div>
                """.formatted(pin);
        sendEmail(email, "Email Verification OTP", html);
    }

    @Override
    public void sendPasswordResetOtp(String email, String otp) {
        String html = """
                <div style="background:#f6f7f2;padding:32px 16px;font-family:Segoe UI,Arial,sans-serif;">
                    <div style="max-width:560px;margin:auto;background:white;border-radius:14px;overflow:hidden;border:1px solid #e5e7eb;">
                        <div style="background:#991b1b;color:white;padding:28px;text-align:center;">
                            <h1 style="margin:0;font-size:24px;">Rice Store</h1>
                            <p style="margin:8px 0 0;color:#fee2e2;">Password reset</p>
                        </div>
                        <div style="padding:32px;">
                            <h2 style="margin-top:0;color:#111827;">Reset your password</h2>
                            <p style="color:#4b5563;line-height:24px;">Use this 6-digit OTP to create a new password.</p>
                            <div style="text-align:center;margin:28px 0;">
                                <span style="display:inline-block;font-size:42px;font-weight:700;letter-spacing:8px;color:#991b1b;background:#fef2f2;border:1px solid #fecaca;border-radius:12px;padding:18px 28px;">%s</span>
                            </div>
                            <p style="color:#b91c1c;font-weight:600;">This OTP expires in 5 minutes.</p>
                            <p style="color:#6b7280;font-size:13px;">If you did not request a password reset, you can ignore this email.</p>
                        </div>
                    </div>
                </div>
                """.formatted(otp);
        sendEmail(email, "Password Reset OTP", html);
    }

    private void sendEmail(String email, String subject, String html) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> body = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", email)),
                "subject", subject,
                "htmlContent", html
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(
                    BREVO_SEND_EMAIL_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
        } catch (Exception e) {
            throw ApiException.badRequest("Unable to send email OTP. Please try again later");
        }
    }
}
