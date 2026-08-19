package com.rice.dto.returnrequest;

import com.rice.entity.enums.RefundMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReturnRequestSubmissionDto {
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Reason is required")
    private String reason;

    private String details;

    @NotNull(message = "Refund method is required")
    private RefundMethod refundMethod;

    @NotEmpty(message = "At least one item must be selected for return")
    @Valid
    private List<ReturnItemRequestDto> items;
}
