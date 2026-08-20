package com.rice.dto.returnrequest;

import com.rice.entity.enums.RefundMethod;
import com.rice.entity.enums.ReturnRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ReturnRequestResponseDto {
    private Long id;
    private String returnNumber;
    private Long orderId;
    private String orderDisplayId;
    private Long customerId;
    private String customerName;
    private ReturnRequestStatus status;
    private String reason;
    private String customerDetails;
    private RefundMethod refundMethod;
    private BigDecimal refundAmount;
    private String paymentDisplay;
    private Instant orderDate;
    private Instant deliveredAt;
    private String adminReason;
    private String adminNote;
    private String returnInstructions;
    private Instant submittedAt;
    private Instant reviewedAt;
    private Instant approvedAt;
    private Instant rejectedAt;
    private Instant refundProcessedAt;
    private Instant receivedAt;
    private String receivedCondition;
    private String receivedNote;
    private String refundReference;
    private String refundNote;
    
    private List<ReturnItemResponseDto> items;
}
