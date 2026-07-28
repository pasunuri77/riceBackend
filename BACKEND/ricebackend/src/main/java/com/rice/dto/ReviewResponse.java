package com.rice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private String productId;
    private String name;
    private Integer rating;
    private String date;
    private String comment;
}
