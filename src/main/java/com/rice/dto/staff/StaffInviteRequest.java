package com.rice.dto.staff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for POST /api/admin/staff/invite
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffInviteRequest {
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Mobile is required")
    private String mobile;
    
    @NotNull(message = "Role is required")
    private String role;  // "admin" or "employee"
}
