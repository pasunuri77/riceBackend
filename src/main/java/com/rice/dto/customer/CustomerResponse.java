package com.rice.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class CustomerResponse {
    private String id;
    private String name;
    private String email;
    private String mobile;
    private long orders;
    private BigDecimal totalSpent;
    private String joined;
    private String status;
}
