package com.rice.dto.staff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for PATCH /api/admin/staff/{id}/role
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeRequest {
    
    @NotBlank(message = "Role is required")
    private String role;  // "admin" or "employee"
}
