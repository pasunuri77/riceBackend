package com.rice.controller;

import com.rice.dto.payment.PaymentResponse;
import com.rice.security.AppUserPrincipal;
import com.rice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/api/admin/payments")
    public List<PaymentResponse> allAdminPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/api/payments")
    public List<PaymentResponse> myPayments(@AuthenticationPrincipal AppUserPrincipal principal) {
        return paymentService.getPaymentsForCustomer(principal.getUser().getId());
    }
}
