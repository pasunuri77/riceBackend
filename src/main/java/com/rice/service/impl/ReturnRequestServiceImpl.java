package com.rice.service.impl;

import com.rice.dto.returnrequest.*;
import com.rice.entity.*;
import com.rice.entity.enums.RefundStatus;
import com.rice.entity.enums.ReturnRequestStatus;
import com.rice.repository.OrderRepository;
import com.rice.repository.RefundRepository;
import com.rice.repository.ReturnRequestItemRepository;
import com.rice.repository.ReturnRequestRepository;
import com.rice.service.ReturnRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final OrderRepository orderRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnRequestItemRepository returnRequestItemRepository;
    private final RefundRepository refundRepository;

    public ReturnRequestServiceImpl(OrderRepository orderRepository,
                                    ReturnRequestRepository returnRequestRepository,
                                    ReturnRequestItemRepository returnRequestItemRepository,
                                    RefundRepository refundRepository) {
        this.orderRepository = orderRepository;
        this.returnRequestRepository = returnRequestRepository;
        this.returnRequestItemRepository = returnRequestItemRepository;
        this.refundRepository = refundRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnableItemsResponseDto getReturnableItems(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Forbidden"); // Should map to 403
        }

        List<ReturnableItemDto> itemDtos = new ArrayList<>();

        for (OrderItem oi : order.getItems()) {
            int returnedQuantity = getReturnedQuantity(oi.getId());
            int returnableQuantity = oi.getQty() - returnedQuantity;

            if (returnableQuantity > 0) {
                BigDecimal unitPrice = oi.getPricePerKgSnapshot().multiply(BigDecimal.valueOf(oi.getWeightKg()));
                
                itemDtos.add(ReturnableItemDto.builder()
                        .orderItemId(oi.getId())
                        .productId(oi.getProduct() != null ? oi.getProduct().getId() : null)
                        .productName(oi.getProductNameSnapshot())
                        .variantName(oi.getWeightKg() + "kg Bag")
                        .unitPrice(unitPrice)
                        .orderedQuantity(oi.getQty())
                        .previouslyReturnedQuantity(returnedQuantity)
                        .returnableQuantity(returnableQuantity)
                        .imageUrl(oi.getImageSnapshot())
                        .build());
            }
        }

        return ReturnableItemsResponseDto.builder()
                .orderId(order.getId())
                .paymentMethod(order.getPaymentMethod())
                .paymentProvider(order.getPaymentProvider())
                .paymentDisplay(order.getPaymentDisplay())
                .items(itemDtos)
                .build();
    }

    @Override
    @Transactional
    public ReturnRequestResponseDto submitReturnRequest(ReturnRequestSubmissionDto dto, User customer) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Forbidden"); // Should map to 403
        }

        if (order.getDeliveredAt() == null) {
            throw new RuntimeException("Order is not delivered yet");
        }

        // Return window validation (30 days)
        if (order.getDeliveredAt().plus(30, ChronoUnit.DAYS).isBefore(Instant.now())) {
            throw new RuntimeException("Return window has expired");
        }

        ReturnRequest returnRequest = ReturnRequest.builder()
                .returnNumber("RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .order(order)
                .customer(customer)
                .reason(dto.getReason())
                .customerDetails(dto.getDetails())
                .refundMethod(dto.getRefundMethod())
                .status(ReturnRequestStatus.PENDING)
                .build();

        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        List<ReturnRequestItem> returnItems = new ArrayList<>();

        for (ReturnItemRequestDto itemDto : dto.getItems()) {
            OrderItem orderItem = order.getItems().stream()
                    .filter(oi -> oi.getId().equals(itemDto.getOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Order item not found in this order"));

            int returnedQuantity = getReturnedQuantity(orderItem.getId());
            int returnableQuantity = orderItem.getQty() - returnedQuantity;

            if (itemDto.getQuantity() > returnableQuantity) {
                throw new RuntimeException("Requested quantity exceeds the remaining returnable quantity"); // 409
            }

            BigDecimal unitPrice = orderItem.getPricePerKgSnapshot().multiply(BigDecimal.valueOf(orderItem.getWeightKg()));
            BigDecimal lineRefund = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));

            totalRefundAmount = totalRefundAmount.add(lineRefund);

            ReturnRequestItem returnItem = ReturnRequestItem.builder()
                    .returnRequest(returnRequest)
                    .orderItem(orderItem)
                    .product(orderItem.getProduct())
                    .productName(orderItem.getProductNameSnapshot())
                    .variantName(orderItem.getWeightKg() + "kg Bag")
                    .unitPrice(unitPrice)
                    .requestedQuantity(itemDto.getQuantity())
                    .refundAmount(lineRefund)
                    .build();

            returnItems.add(returnItem);
        }

        returnRequest.setItems(returnItems);
        returnRequest.setRefundAmount(totalRefundAmount);
        
        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestResponseDto> getCustomerReturnRequests(Long customerId) {
        return returnRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestResponseDto> getAllReturnRequests(String status) {
        List<ReturnRequest> requests;
        if (status != null && !status.equalsIgnoreCase("all")) {
            ReturnRequestStatus requestStatus = ReturnRequestStatus.valueOf(status.toUpperCase());
            requests = returnRequestRepository.findByStatusOrderByCreatedAtDesc(requestStatus);
        } else {
            requests = returnRequestRepository.findAll();
        }
        
        return requests.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResponseDto getReturnRequestByIdForCustomer(Long id, Long customerId) {
        ReturnRequest request = returnRequestRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new RuntimeException("Return request not found"));
        return mapToResponseDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResponseDto getReturnRequestByIdForAdmin(Long id) {
        ReturnRequest request = returnRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Return request not found"));
        return mapToResponseDto(request);
    }

    @Override
    @Transactional
    public ReturnRequestResponseDto approveReturnRequest(Long id, ApproveReturnDto dto) {
        ReturnRequest request = returnRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Return request not found"));

        if (request.getStatus() != ReturnRequestStatus.PENDING) {
            throw new RuntimeException("Return request is not pending");
        }

        request.setStatus(ReturnRequestStatus.APPROVED);
        request.setAdminNote(dto.getAdminNote());
        request.setReturnInstructions(dto.getReturnInstructions());
        request.setApprovedAt(Instant.now());

        // Validate quantities again and set approved quantities
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        
        for (ReturnRequestItem item : request.getItems()) {
            // Re-calculate returnable excluding the current request to avoid double counting if it was already marked,
            // but since it's PENDING it's currently counted in getReturnedQuantity. We only approve what they requested.
            item.setApprovedQuantity(item.getRequestedQuantity());
            
            BigDecimal lineRefund = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getApprovedQuantity()));
            item.setRefundAmount(lineRefund);
            totalRefundAmount = totalRefundAmount.add(lineRefund);
        }
        
        request.setRefundAmount(totalRefundAmount);

        Refund refund = Refund.builder()
                .returnRequest(request)
                .order(request.getOrder())
                .paymentMethod(request.getRefundMethod())
                .amount(totalRefundAmount)
                .status(RefundStatus.PENDING)
                .build();
        
        refundRepository.save(refund);
        
        return mapToResponseDto(returnRequestRepository.save(request));
    }

    @Override
    @Transactional
    public ReturnRequestResponseDto rejectReturnRequest(Long id, RejectReturnDto dto) {
        ReturnRequest request = returnRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Return request not found"));

        if (request.getStatus() != ReturnRequestStatus.PENDING) {
            throw new RuntimeException("Return request is not pending");
        }

        request.setStatus(ReturnRequestStatus.REJECTED);
        request.setAdminReason(dto.getReason());
        request.setAdminNote(dto.getAdminNote());
        request.setRejectedAt(Instant.now());

        return mapToResponseDto(returnRequestRepository.save(request));
    }

    private int getReturnedQuantity(Long orderItemId) {
        List<ReturnRequestItem> returnItems = returnRequestItemRepository.findActiveReturnsByOrderItemId(orderItemId);
        return returnItems.stream()
                .mapToInt(ReturnRequestItem::getRequestedQuantity)
                .sum();
    }

    private ReturnRequestResponseDto mapToResponseDto(ReturnRequest entity) {
        List<ReturnItemResponseDto> itemDtos = entity.getItems().stream()
                .map(item -> ReturnItemResponseDto.builder()
                        .id(item.getId())
                        .orderItemId(item.getOrderItem().getId())
                        .productName(item.getProductName())
                        .variantName(item.getVariantName())
                        .unitPrice(item.getUnitPrice())
                        .requestedQuantity(item.getRequestedQuantity())
                        .approvedQuantity(item.getApprovedQuantity())
                        .refundAmount(item.getRefundAmount())
                        .build())
                .collect(Collectors.toList());

        return ReturnRequestResponseDto.builder()
                .id(entity.getId())
                .returnNumber(entity.getReturnNumber())
                .orderId(entity.getOrder().getId())
                .customerId(entity.getCustomer().getId())
                .status(entity.getStatus())
                .reason(entity.getReason())
                .customerDetails(entity.getCustomerDetails())
                .refundMethod(entity.getRefundMethod())
                .refundAmount(entity.getRefundAmount())
                .adminReason(entity.getAdminReason())
                .adminNote(entity.getAdminNote())
                .returnInstructions(entity.getReturnInstructions())
                .submittedAt(entity.getSubmittedAt())
                .reviewedAt(entity.getReviewedAt())
                .approvedAt(entity.getApprovedAt())
                .rejectedAt(entity.getRejectedAt())
                .refundProcessedAt(entity.getRefundProcessedAt())
                .items(itemDtos)
                .build();
    }
}
