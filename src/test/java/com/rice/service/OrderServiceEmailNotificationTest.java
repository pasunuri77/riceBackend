package com.rice.service;

import com.rice.email.EmailService;
import com.rice.dto.order.OrderCreateRequest;
import com.rice.dto.order.OrderItemRequest;
import com.rice.entity.Order;
import com.rice.entity.Product;
import com.rice.entity.User;
import com.rice.entity.enums.DeliveryStatus;
import com.rice.entity.enums.Role;
import com.rice.repository.OrderRepository;
import com.rice.repository.ProductRepository;
import com.rice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
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
    private UserRepository userRepository;
    private OrderService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);
        storeSettingsService = mock(StoreSettingsService.class);
        couponService = mock(CouponService.class);
        emailService = mock(EmailService.class);
        productAnalyticsService = mock(ProductAnalyticsService.class);
        userRepository = mock(UserRepository.class);
        service = new OrderService(orderRepository, productRepository, storeSettingsService, couponService, emailService, productAnalyticsService, userRepository);
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

    @Test
    void create_sendsOrderPlacedEmail() {
        User customer = User.builder().id(21L).name("Eve").email("eve@example.com").role(Role.USER).build();
        var item = new OrderItemRequest();
        item.setId("prod-1");
        item.setName("Basmati Rice");
        item.setImage("image.png");
        item.setPricePerKg(new BigDecimal("120.00"));
        item.setWeight(5);
        item.setQty(2);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setAddress("123 Main St");
        req.setPaymentMethod("COD");
        req.setItems(List.of(item));

        Product product = Product.builder().id("prod-1").stock(20).build();
        when(productRepository.findByIdForUpdate("prod-1")).thenReturn(Optional.of(product));
        when(storeSettingsService.current()).thenReturn(new com.rice.entity.StoreSettings());

        Order order = Order.builder()
                .id(101L)
                .customer(customer)
                .addressSnapshot("123 Main St")
                .amount(new BigDecimal("1240.00"))
                .items(List.of())
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        service.create(customer, req);

        verify(emailService).sendOrderPlaced(eq("eve@example.com"), any(Order.class));
    }
}
