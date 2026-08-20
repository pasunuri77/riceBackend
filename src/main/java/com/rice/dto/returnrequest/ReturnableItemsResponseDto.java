package com.rice.dto.returnrequest;

import com.rice.entity.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReturnableItemsResponseDto {
    private Long orderId;
    private String orderDisplayId;
    private PaymentMethod paymentMethod;
    private String paymentProvider;
    private String paymentDisplay;
    private List<ReturnableItemDto> items;
}
