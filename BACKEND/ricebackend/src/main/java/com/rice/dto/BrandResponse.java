package com.rice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BrandResponse {
    private String id;
    private String name;
    private String color;
    private String logo;
    private long products;
}
