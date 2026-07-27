package com.rice.dto.customer;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {
    @NotBlank
    private String status; // "Active" or "Blocked"
}
