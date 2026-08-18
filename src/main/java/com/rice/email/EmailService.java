package com.rice.email;

import com.rice.entity.Order;

public interface EmailService {

    void sendWelcomeEmail(String email, String name);

    void sendPin(
            String email,
            String pin);

    void sendPasswordResetOtp(
            String email,
            String otp);

    void sendOrderAcceptedEmail(
            String email,
            Order order);

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


    /**
     * Send order confirmation notification to admin
     */
    void sendAdminOrderConfirmedNotification(String customerName, String orderId);

    /**
     * Send order shipped notification to admin
     */
    void sendAdminOrderShippedNotification(String customerName, String orderId);

    /**
     * Send order delivered notification to admin
     */
    void sendAdminOrderDeliveredNotification(String customerName, String orderId);

    /**
     * Send order cancelled notification to admin
     */
    void sendAdminOrderCancelledNotification(String customerName, String orderId);
}
