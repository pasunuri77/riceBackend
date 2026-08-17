package com.rice.email;

import com.rice.entity.enums.DeliveryStatus;
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

    @Override
    public void sendOrderStatusUpdate(String email, String customerName, String orderId, DeliveryStatus status) {
        String safeName = escapeHtml(customerName);
        String safeOrderId = escapeHtml(orderId);
        OrderStatusCopy copy = copyFor(status, safeName, safeOrderId);
        String html = """
                <div style="background:#f6f7f2;padding:32px 16px;font-family:Segoe UI,Arial,sans-serif;">
                    <div style="max-width:560px;margin:auto;background:white;border-radius:14px;overflow:hidden;border:1px solid #e5e7eb;">
                        <div style="background:%s;color:white;padding:28px;text-align:center;">
                            <h1 style="margin:0;font-size:24px;">RiceBazaar</h1>
                            <p style="margin:8px 0 0;">%s</p>
                        </div>
                        <div style="padding:32px;">
                            <h2 style="margin-top:0;color:#111827;">%s</h2>
                            <p style="color:#4b5563;line-height:24px;">%s</p>
                            <div style="margin:24px 0;padding:16px;background:#f9fafb;border:1px solid #e5e7eb;border-radius:10px;">
                                <p style="margin:0;color:#6b7280;font-size:13px;">Order</p>
                                <p style="margin:4px 0 0;color:#111827;font-weight:700;font-size:18px;">%s</p>
                            </div>
                            <p style="color:#6b7280;font-size:13px;">You can track this order anytime from the "My Orders" section of your RiceBazaar account.</p>
                        </div>
                    </div>
                </div>
                """.formatted(copy.color, copy.eyebrow, copy.heading, copy.body, safeOrderId);
        sendEmail(email, copy.subject, html);
    }

    private record OrderStatusCopy(String color, String eyebrow, String heading, String body, String subject) {}

    private OrderStatusCopy copyFor(DeliveryStatus status, String customerName, String orderId) {
        return switch (status) {
            case PROCESSING -> new OrderStatusCopy(
                    "#1d4ed8", "Order update",
                    "Your order is being processed",
                    "Hi " + customerName + ", we've started processing your order " + orderId + ". We'll email you again as soon as it ships.",
                    "Your RiceBazaar order " + orderId + " is being processed");
            case SHIPPED -> new OrderStatusCopy(
                    "#b45309", "Order update",
                    "Your order has shipped",
                    "Hi " + customerName + ", your order " + orderId + " is on its way to you.",
                    "Your RiceBazaar order " + orderId + " has shipped");
            case DELIVERED -> new OrderStatusCopy(
                    "#166534", "Order update",
                    "Your order has been delivered",
                    "Hi " + customerName + ", your order " + orderId + " has been delivered. We hope you enjoy your rice!",
                    "Your RiceBazaar order " + orderId + " has been delivered");
            case CANCELLED -> new OrderStatusCopy(
                    "#991b1b", "Order update",
                    "Your order has been cancelled",
                    "Hi " + customerName + ", your order " + orderId + " has been cancelled. If you didn't request this or have questions, please contact us.",
                    "Your RiceBazaar order " + orderId + " has been cancelled");
            case PENDING -> throw new IllegalArgumentException("PENDING is not a notifiable order status transition");
        };
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
