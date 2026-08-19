package com.rice.service;

import com.rice.dto.delivery.ServiceabilityResponse;
import com.rice.dto.order.OrderCreateRequest;
import com.rice.dto.order.OrderItemRequest;
import com.rice.entity.User;
import com.rice.entity.enums.Role;
import com.rice.exception.ApiException;
import com.rice.repository.OrderRepository;
import com.rice.repository.ProductRepository;
import com.rice.repository.UserRepository;
import com.rice.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderServiceValidationTest {

    private OrderService service;
    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        StoreSettingsService storeSettingsService = mock(StoreSettingsService.class);
        CouponService couponService = mock(CouponService.class);
        EmailService emailService = mock(EmailService.class);
        ProductAnalyticsService productAnalyticsService = mock(ProductAnalyticsService.class);
        UserRepository userRepository = mock(UserRepository.class);
        deliveryService = mock(DeliveryService.class);
        service = new OrderService(orderRepository, productRepository, storeSettingsService, couponService, emailService, productAnalyticsService, userRepository, deliveryService);
    }

    @Test
    void create_failsWhenZipCodeIsNull() {
        User customer = User.builder().id(1L).role(Role.USER).build();
        OrderCreateRequest req = new OrderCreateRequest();
        req.setItems(List.of(new OrderItemRequest()));
        
        assertThrows(ApiException.class, () -> service.create(customer, req), "Delivery ZIP code is required");
    }

    @Test
    void create_failsWhenZipCodeIsInvalid() {
        User customer = User.builder().id(1L).role(Role.USER).build();
        OrderCreateRequest req = new OrderCreateRequest();
        req.setDeliveryZipCode("99999");
        req.setItems(List.of(new OrderItemRequest()));

        when(deliveryService.checkDelivery("99999")).thenReturn(ServiceabilityResponse.builder().deliverable(false).build());
        
        assertThrows(ApiException.class, () -> service.create(customer, req), "Delivery is not available for ZIP code 99999");
    }
}
