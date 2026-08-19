package com.rice.dto.returnrequest;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ReturnableItemDto {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private String variantName;
    private BigDecimal unitPrice;
    private Integer orderedQuantity;
    private Integer previouslyReturnedQuantity;
    private Integer returnableQuantity;
    private String imageUrl;
}
