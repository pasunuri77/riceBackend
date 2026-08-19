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
    private Long customerId;
    private ReturnRequestStatus status;
    private String reason;
    private String customerDetails;
    private RefundMethod refundMethod;
    private BigDecimal refundAmount;
    private String adminReason;
    private String adminNote;
    private String returnInstructions;
    private Instant submittedAt;
    private Instant reviewedAt;
    private Instant approvedAt;
    private Instant rejectedAt;
    private Instant refundProcessedAt;
    
    private List<ReturnItemResponseDto> items;
}
