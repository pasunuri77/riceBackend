package com.rice.controller;

import com.rice.dto.order.OrderCreateRequest;
import com.rice.dto.order.OrderResponse;
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
    public OrderResponse getById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @PostMapping("/api/orders")
    public OrderResponse create(@AuthenticationPrincipal AppUserPrincipal principal, @Valid @RequestBody OrderCreateRequest request) {
        return orderService.create(principal.getUser(), request);
    }

    @GetMapping("/api/admin/orders")
    public List<OrderResponse> all() {
        return orderService.listAll();
    }
}
