package com.rice.dto.banner;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class BannerResponse {
    private Long id;
    private String title;
    private String subtitle;
    private String imageUrl;
    private String linkTo;
    private Boolean active;
    private Integer sortOrder;
    private Instant createdAt;
    private Instant updatedAt;
}
