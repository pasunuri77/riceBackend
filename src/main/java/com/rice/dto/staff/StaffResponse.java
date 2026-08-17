package com.rice.dto.staff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Response for listing staff (admin/employee users).
 */
@Getter
@Builder
@AllArgsConstructor
public class StaffResponse {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String role;  // "admin" or "employee"
    private String status;  // "ACTIVE" or "INACTIVE"
    private String joined;
    private boolean pendingSetup;  // true if user hasn't set their password yet
}
