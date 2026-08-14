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

    @Value("${app.contact.support-email:${app.email.sender-email:pasunurisagar2001@gmail.com}}")
    private String supportEmail;

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

    @Override
    public void sendOrderAcceptedEmail(String email, String customerName, String orderId) {
        String safeName = escapeHtml(customerName == null ? "Customer" : customerName);
        String safeOrderId = escapeHtml(orderId == null ? "" : orderId);
        String html = """
                <div style="background:#f6f7f2;padding:32px 16px;font-family:Segoe UI,Arial,sans-serif;">
                    <div style="max-width:600px;margin:auto;background:white;border-radius:14px;overflow:hidden;border:1px solid #e5e7eb;">
                        <div style="background:#166534;color:white;padding:28px;text-align:center;">
                            <h1 style="margin:0;font-size:24px;">Rice Store</h1>
                            <p style="margin:8px 0 0;color:#dcfce7;">Order accepted</p>
                        </div>
                        <div style="padding:32px;color:#111827;line-height:24px;">
                            <h2 style="margin-top:0;">Hello %s,</h2>
                            <p>Your order <strong>%s</strong> has been accepted by our admin team and is now being processed.</p>
                            <p>We will notify you again when your order is shipped and delivered.</p>
                            <p style="margin-top:24px;color:#4b5563;">Thank you for shopping with Rice Store.</p>
                        </div>
                    </div>
                </div>
                """.formatted(safeName, safeOrderId);
        sendEmail(email, "Your order has been accepted", html);
    }

    @Override
    public void sendOrderShippedEmail(String email, String customerName, String orderId) {
        String safeName = escapeHtml(customerName == null ? "Customer" : customerName);
        String safeOrderId = escapeHtml(orderId == null ? "" : orderId);
        String html = """
                <div style="background:#f6f7f2;padding:32px 16px;font-family:Segoe UI,Arial,sans-serif;">
                    <div style="max-width:600px;margin:auto;background:white;border-radius:14px;overflow:hidden;border:1px solid #e5e7eb;">
                        <div style="background:#1d4ed8;color:white;padding:28px;text-align:center;">
                            <h1 style="margin:0;font-size:24px;">Rice Store</h1>
                            <p style="margin:8px 0 0;color:#dbeafe;">Your order is on the way</p>
                        </div>
                        <div style="padding:32px;color:#111827;line-height:24px;">
                            <h2 style="margin-top:0;">Hello %s,</h2>
                            <p>Your order <strong>%s</strong> has been shipped and is on its way to you.</p>
                            <p>You will receive another update when it is delivered.</p>
                            <p style="margin-top:24px;color:#4b5563;">Thank you for shopping with Rice Store.</p>
                        </div>
                    </div>
                </div>
                """.formatted(safeName, safeOrderId);
        sendEmail(email, "Your order has been shipped", html);
    }

    @Override
    public void sendOrderDeliveredEmail(String email, String customerName, String orderId) {
        String safeName = escapeHtml(customerName == null ? "Customer" : customerName);
        String safeOrderId = escapeHtml(orderId == null ? "" : orderId);
        String html = """
                <div style="background:#f6f7f2;padding:32px 16px;font-family:Segoe UI,Arial,sans-serif;">
                    <div style="max-width:600px;margin:auto;background:white;border-radius:14px;overflow:hidden;border:1px solid #e5e7eb;">
                        <div style="background:#0f766e;color:white;padding:28px;text-align:center;">
                            <h1 style="margin:0;font-size:24px;">Rice Store</h1>
                            <p style="margin:8px 0 0;color:#ccfbf1;">Order delivered</p>
                        </div>
                        <div style="padding:32px;color:#111827;line-height:24px;">
                            <h2 style="margin-top:0;">Hello %s,</h2>
                            <p>Your order <strong>%s</strong> has been delivered successfully.</p>
                            <p>We hope you enjoy your purchase. Please contact us if you need any assistance.</p>
                            <p style="margin-top:24px;color:#4b5563;">Thank you for shopping with Rice Store.</p>
                        </div>
                    </div>
                </div>
                """.formatted(safeName, safeOrderId);
        sendEmail(email, "Your order has been delivered", html);
    }

    @Override
    public void sendOrderCancelledEmail(String email, String customerName, String orderId) {
        String safeName = escapeHtml(customerName == null ? "Customer" : customerName);
        String safeOrderId = escapeHtml(orderId == null ? "" : orderId);
        String html = """
                <div style="background:#f6f7f2;padding:32px 16px;font-family:Segoe UI,Arial,sans-serif;">
                    <div style="max-width:600px;margin:auto;background:white;border-radius:14px;overflow:hidden;border:1px solid #e5e7eb;">
                        <div style="background:#991b1b;color:white;padding:28px;text-align:center;">
                            <h1 style="margin:0;font-size:24px;">Rice Store</h1>
                            <p style="margin:8px 0 0;color:#fee2e2;">Order cancelled</p>
                        </div>
                        <div style="padding:32px;color:#111827;line-height:24px;">
                            <h2 style="margin-top:0;">Hello %s,</h2>
                            <p>Your order <strong>%s</strong> has been cancelled.</p>
                            <p>If this was not intentional, please contact support for assistance.</p>
                            <p style="margin-top:24px;color:#4b5563;">Thank you for shopping with Rice Store.</p>
                        </div>
                    </div>
                </div>
                """.formatted(safeName, safeOrderId);
        sendEmail(email, "Your order has been cancelled", html);
    }

    @Override
    public void sendContactMessage(String name, String email, String subject, String message) {
        String safeName = escapeHtml(name);
        String safeEmail = escapeHtml(email);
        String safeSubject = escapeHtml(subject);
        String safeMessage = escapeHtml(message).replace("\n", "<br>");
        String html = """
                <div style="background:#f6f7f2;padding:32px 16px;font-family:Segoe UI,Arial,sans-serif;">
                    <div style="max-width:640px;margin:auto;background:white;border-radius:14px;overflow:hidden;border:1px solid #e5e7eb;">
                        <div style="background:#b45309;color:white;padding:24px;">
                            <h1 style="margin:0;font-size:22px;">New RiceBazaar Contact Message</h1>
                            <p style="margin:8px 0 0;color:#ffedd5;">Submitted from the Contact Us page</p>
                        </div>
                        <div style="padding:28px;color:#111827;">
                            <p><strong>Name:</strong> %s</p>
                            <p><strong>Email:</strong> %s</p>
                            <p><strong>Subject:</strong> %s</p>
                            <div style="margin-top:24px;">
                                <p style="margin-bottom:8px;"><strong>Message:</strong></p>
                                <div style="background:#fff7ed;border:1px solid #fed7aa;border-radius:10px;padding:16px;line-height:24px;">%s</div>
                            </div>
                        </div>
                    </div>
                </div>
                """.formatted(safeName, safeEmail, safeSubject, safeMessage);
        sendEmail(supportEmail, "RiceBazaar Contact: " + subject, html, name, email);
    }

    private void sendEmail(String email, String subject, String html) {
        sendEmail(email, subject, html, null, null);
    }

    private void sendEmail(String email, String subject, String html, String replyToName, String replyToEmail) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("sender", Map.of("name", senderName, "email", senderEmail));
        body.put("to", List.of(Map.of("email", email)));
        body.put("subject", subject);
        body.put("htmlContent", html);
        if (replyToEmail != null && !replyToEmail.isBlank()) {
            body.put("replyTo", Map.of("name", replyToName, "email", replyToEmail));
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(
                    BREVO_SEND_EMAIL_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
        } catch (Exception e) {
            throw ApiException.badRequest("Unable to send email. Please try again later");
        }
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
