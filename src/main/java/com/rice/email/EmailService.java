package com.rice.email;

public interface EmailService {

    void sendWelcomeEmail(String email, String name);

    void sendPin(
            String email,
            String pin);

    void sendPasswordResetOtp(
            String email,
            String otp);

    void sendOrderPlaced(String email, String customerName, String orderId, java.math.BigDecimal amount);

    void sendOrderAcceptedEmail(
            String email,
            String customerName,
            String orderId);

    void sendOrderShippedEmail(
            String email,
            String customerName,
            String orderId);

    void sendOrderDeliveredEmail(
            String email,
            String customerName,
            String orderId);

    void sendOrderCancelledEmail(
            String email,
            String customerName,
            String orderId);

    void sendContactMessage(
            String name,
            String email,
            String subject,
            String message);

    /**
     * Send password setup link for newly invited staff/customers
     */
    void sendInvitationEmail(String email, String name, String resetLink, String role);
}
