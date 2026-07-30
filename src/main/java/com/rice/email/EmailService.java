package com.rice.email;

public interface EmailService {

    void sendPin(
            String email,
            String pin);

    void sendPasswordResetOtp(
            String email,
            String otp);

    void sendContactMessage(
            String name,
            String email,
            String subject,
            String message);
}
