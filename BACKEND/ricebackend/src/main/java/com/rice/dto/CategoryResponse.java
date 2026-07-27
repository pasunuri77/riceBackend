package com.rice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CategoryResponse {
    private String id;
    private String name;
    private String icon;
    private String image;
    private long count;
}
