package com.rice.dto.staff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for PATCH /api/admin/staff/{id}/status
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusChangeRequest {
    
    @NotBlank(message = "Status is required")
    private String status;  // "ACTIVE" or "INACTIVE"
}
