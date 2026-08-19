package com.rice.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DeliveryAreaResponse {
    private Long id;
    private String name;
    private String description;
    private List<String> zipCodes;
    private boolean isNamedZone;
}
