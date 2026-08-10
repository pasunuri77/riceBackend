package com.rice.controller;

import com.rice.dto.order.OrderCreateRequest;
import com.rice.dto.order.OrderResponse;
import com.rice.dto.order.UpdateOrderDeliveryInfoRequest;
import com.rice.dto.order.UpdateOrderStatusRequest;
import com.rice.security.AppUserPrincipal;
import com.rice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/api/orders")
    public List<OrderResponse> mine(@AuthenticationPrincipal AppUserPrincipal principal) {
        return orderService.listByCustomer(principal.getUser().getId());
    }

    @GetMapping("/api/orders/{id}")
    public OrderResponse getById(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        return orderService.getById(id, principal.getUser());
    }

    @PostMapping("/api/orders")
    public OrderResponse create(@AuthenticationPrincipal AppUserPrincipal principal, @Valid @RequestBody OrderCreateRequest request) {
        return orderService.create(principal.getUser(), request);
    }

    @PatchMapping("/api/orders/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable String id) {
        return orderService.cancel(id, principal.getUser());
    }

    @GetMapping("/api/admin/orders")
    public List<OrderResponse> all() {
        return orderService.listAll();
    }

    @PatchMapping("/api/admin/orders/{id}/payment-status")
    public OrderResponse updatePaymentStatus(@PathVariable String id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updatePaymentStatus(id, request.getStatus());
    }

    @PatchMapping("/api/admin/orders/{id}/delivery-status")
    public OrderResponse updateDeliveryStatus(@PathVariable String id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateDeliveryStatus(id, request.getStatus());
    }

    @PatchMapping("/api/admin/orders/{id}/delivery-info")
    public OrderResponse updateDeliveryInfo(@PathVariable String id, @Valid @RequestBody com.rice.dto.order.UpdateOrderDeliveryInfoRequest request) {
        return orderService.updateDeliveryInfo(id, request);
    }
}
