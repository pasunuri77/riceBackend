package com.rice.service;

import com.rice.email.EmailServiceImpl;
import com.rice.entity.Order;
import com.rice.entity.OrderItem;
import com.rice.entity.User;
import com.rice.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EmailServiceTest {

    @Test
    void sendOrderPlaced_doesNotThrowFormatError() {
        EmailServiceImpl emailService = new EmailServiceImpl();
        ReflectionTestUtils.setField(emailService, "apiKey", "test-key");
        ReflectionTestUtils.setField(emailService, "senderName", "Rice Store");
        ReflectionTestUtils.setField(emailService, "senderEmail", "test@example.com");
        ReflectionTestUtils.setField(emailService, "frontendBaseUrl", "http://localhost:5173");

        User customer = User.builder()
                .id(1L)
                .name("Sagar")
                .email("sagar@example.com")
                .role(Role.USER)
                .build();

        OrderItem item = OrderItem.builder()
                .productNameSnapshot("Sona Masoori Rice 10% Broken")
                .weightKg(5)
                .qty(2)
                .pricePerKgSnapshot(new BigDecimal("120.00"))
                .build();

        Order order = Order.builder()
                .id(12345L)
                .customer(customer)
                .createdAt(Instant.now())
                .subtotal(new BigDecimal("1200.00"))
                .deliveryCharge(new BigDecimal("40.00"))
                .tax(new BigDecimal("0.00"))
                .discountAmount(new BigDecimal("50.00"))
                .amount(new BigDecimal("1190.00"))
                .addressSnapshot("Flat 101, Test Street, Hyderabad 500001")
                .items(List.of(item))
                .build();

        // The RestTemplate network call will fail in unit test because api-key is fake, but formatting must succeed without UnknownFormatConversionException
        try {
            emailService.sendOrderAcceptedEmail("sagar@example.com", order);
        } catch (Exception ex) {
            // Should not be UnknownFormatConversionException
            org.junit.jupiter.api.Assertions.assertFalse(
                    ex instanceof java.util.UnknownFormatConversionException,
                    "String formatting failed: " + ex.getMessage()
            );
        }
    }
}
