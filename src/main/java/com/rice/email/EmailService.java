package com.rice.email;

import com.rice.entity.enums.DeliveryStatus;

public interface EmailService {

    void sendPin(
            String email,
            String pin);

    void sendPasswordResetOtp(
            String email,
            String otp);

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

    // Sent from OrderService whenever an order's delivery status transitions into
    // PROCESSING, SHIPPED, DELIVERED, or CANCELLED (both the admin-driven status
    // update and the customer-initiated cancel path). PENDING is the initial
    // state on order creation, not a transition, so it's intentionally not covered.
    void sendOrderStatusUpdate(
            String email,
            String customerName,
            String orderId,
            DeliveryStatus status);
}
