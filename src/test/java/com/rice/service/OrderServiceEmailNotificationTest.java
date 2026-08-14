package com.rice.service;

import com.rice.email.EmailService;
import com.rice.entity.Order;
import com.rice.entity.User;
import com.rice.entity.enums.DeliveryStatus;
import com.rice.entity.enums.Role;
import com.rice.entity.enums.DeliveryStatus;
import com.rice.repository.OrderRepository;
import com.rice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderServiceEmailNotificationTest {

    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private StoreSettingsService storeSettingsService;
    private CouponService couponService;
    private EmailService emailService;
    private ProductAnalyticsService productAnalyticsService;
    private OrderService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);
        storeSettingsService = mock(StoreSettingsService.class);
        couponService = mock(CouponService.class);
        emailService = mock(EmailService.class);
        productAnalyticsService = mock(ProductAnalyticsService.class);
        service = new OrderService(orderRepository, productRepository, storeSettingsService, couponService, emailService, productAnalyticsService);
    }

    @Test
    void updateDeliveryStatus_toProcessingSendsOrderAcceptedEmail() {
        User customer = User.builder().id(11L).name("Alice").email("alice@example.com").role(Role.USER).build();
        Order order = Order.builder().id(42L).customer(customer).deliveryStatus(DeliveryStatus.PENDING).build();
        when(orderRepository.findById(42L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        service.updateDeliveryStatus("ORD10042", "PROCESSING");

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> orderIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendOrderAcceptedEmail(emailCaptor.capture(), nameCaptor.capture(), orderIdCaptor.capture());
        assertEquals("alice@example.com", emailCaptor.getValue());
        assertEquals("Alice", nameCaptor.getValue());
        assertEquals("ORD10042", orderIdCaptor.getValue());
    }

    @Test
    void updateDeliveryStatus_toShippedSendsOrderShippedEmail() {
        User customer = User.builder().id(12L).name("Bob").email("bob@example.com").role(Role.USER).build();
        Order order = Order.builder().id(43L).customer(customer).deliveryStatus(DeliveryStatus.PROCESSING).build();
        when(orderRepository.findById(43L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        service.updateDeliveryStatus("ORD10043", "SHIPPED");

        verify(emailService).sendOrderShippedEmail("bob@example.com", "Bob", "ORD10043");
    }

    @Test
    void updateDeliveryStatus_toDeliveredSendsOrderDeliveredEmail() {
        User customer = User.builder().id(13L).name("Cara").email("cara@example.com").role(Role.USER).build();
        Order order = Order.builder().id(44L).customer(customer).deliveryStatus(DeliveryStatus.SHIPPED).build();
        when(orderRepository.findById(44L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        service.updateDeliveryStatus("ORD10044", "DELIVERED");

        verify(emailService).sendOrderDeliveredEmail("cara@example.com", "Cara", "ORD10044");
    }

    @Test
    void updateDeliveryStatus_toCancelledSendsOrderCancelledEmail() {
        User customer = User.builder().id(14L).name("Dana").email("dana@example.com").role(Role.USER).build();
        Order order = Order.builder().id(45L).customer(customer).deliveryStatus(DeliveryStatus.PENDING).build();
        when(orderRepository.findById(45L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        service.updateDeliveryStatus("ORD10045", "CANCELLED");

        verify(emailService).sendOrderCancelledEmail("dana@example.com", "Dana", "ORD10045");
    }
}
