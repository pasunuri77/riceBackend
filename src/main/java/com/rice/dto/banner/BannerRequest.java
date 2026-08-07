package com.rice.dto.banner;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BannerRequest {
    @NotBlank
    private String title;

    private String subtitle;

    private String imageUrl;

    private String linkTo;

    private Boolean active;

    private Integer sortOrder;
}
