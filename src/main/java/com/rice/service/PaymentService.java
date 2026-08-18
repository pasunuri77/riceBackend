package com.rice.service;

import com.rice.dto.payment.PaymentResponse;
import com.rice.entity.Order;
import com.rice.entity.Payment;
import com.rice.repository.OrderRepository;
import com.rice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsForCustomer(Long customerId) {
        // Find orders for customer, then we could filter payments by those orders.
        // Or we just get all payments and filter (not optimal for huge DBs, but simple for now)
        // Alternatively, since Payment entity lacks customerId, we'll fetch customer's orders and filter by orderId.
        List<Long> customerOrderIds = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(Order::getId)
                .toList();

        return paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(p -> customerOrderIds.contains(p.getOrderId()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse toResponse(Payment payment) {
        Order order = orderRepository.findById(payment.getOrderId()).orElse(null);
        String displayId = "";
        if (order != null) {
            String prefix = "offline".equalsIgnoreCase(order.getOrderType()) ? "OFF-" : "RBZ-";
            int year = order.getCreatedAt() != null ? order.getCreatedAt().atZone(ZoneOffset.UTC).getYear() : Year.now().getValue();
            displayId = prefix + year + "-" + (10000 + order.getId());
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderType(payment.getOrderType())
                .orderId(payment.getOrderId())
                .displayOrderId(displayId)
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
