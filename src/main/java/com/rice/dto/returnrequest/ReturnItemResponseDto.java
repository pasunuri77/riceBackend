package com.rice.dto.returnrequest;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ReturnItemResponseDto {
    private Long id;
    private Long orderItemId;
    private String productName;
    private String variantName;
    private BigDecimal unitPrice;
    private Integer requestedQuantity;
    private Integer approvedQuantity;
    private BigDecimal refundAmount;
}
