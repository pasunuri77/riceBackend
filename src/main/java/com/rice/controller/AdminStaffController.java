package com.rice.controller;

import com.rice.dto.staff.PermissionsResponse;
import com.rice.dto.staff.RoleChangeRequest;
import com.rice.dto.staff.StaffInviteRequest;
import com.rice.dto.staff.StaffResponse;
import com.rice.dto.staff.StatusChangeRequest;
import com.rice.security.AppUserPrincipal;
import com.rice.service.EmployeePermissionService;
import com.rice.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Admin controller for managing staff (admin/employee) accounts and their permissions.
 */
@RestController
@RequiredArgsConstructor
public class AdminStaffController {

    private final StaffService staffService;
    private final EmployeePermissionService permissionService;

    /**
     * GET /api/admin/staff - List all staff (admin and employee users)
     */
    @GetMapping("/api/admin/staff")
    public List<StaffResponse> listAll() {
        return staffService.listAll();
    }

    /**
     * GET /api/admin/staff/{id} - Get a single staff member
     */
    @GetMapping("/api/admin/staff/{id}")
    public StaffResponse getById(@PathVariable Long id) {
        return staffService.getById(id);
    }

    /**
     * POST /api/admin/staff/invite - Invite a new staff member (admin or employee)
     * Generates a password-reset token and sends invitation email
     */
    @PostMapping("/api/admin/staff/invite")
    public StaffResponse invite(@Valid @RequestBody StaffInviteRequest request) {
        return staffService.invite(request);
    }

    /**
     * PATCH /api/admin/staff/{id}/role - Change a staff member's role
     * If promoting to employee, creates blank permission record if needed
     */
    @PatchMapping("/api/admin/staff/{id}/role")
    public StaffResponse changeRole(@PathVariable Long id, @Valid @RequestBody RoleChangeRequest request) {
        return staffService.changeRole(id, request.getRole());
    }

    /**
     * PATCH /api/admin/staff/{id}/status - Change a staff member's status (ACTIVE/INACTIVE)
     */
    @PatchMapping("/api/admin/staff/{id}/status")
    public StaffResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest request) {
        return staffService.changeStatus(id, request.getStatus());
    }

    /**
     * DELETE /api/admin/staff/{id} - Delete a staff account
     * Enforces role hierarchy: nobody can delete their own account, admin can delete anyone,
     * employee can only delete customer accounts
     */
    @DeleteMapping("/api/admin/staff/{id}")
    public void deleteStaff(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        staffService.deleteStaffAccount(id, principal.getUser());
    }

    /**
     * GET /api/admin/staff/{id}/permissions - Get employee permissions
     */
    @GetMapping("/api/admin/staff/{id}/permissions")
    public PermissionsResponse getPermissions(@PathVariable Long id) {
        return permissionService.getPermissions(id);
    }

    /**
     * PUT /api/admin/staff/{id}/permissions - Update employee permissions
     */
    @PutMapping("/api/admin/staff/{id}/permissions")
    public PermissionsResponse updatePermissions(@PathVariable Long id, @Valid @RequestBody PermissionsResponse request) {
        return permissionService.updatePermissions(id, request);
    }

    /**
     * GET /api/staff/my-permissions - Get current user's permissions (self-service)
     * Used by employee dashboard to decide what to show
     */
    @GetMapping("/api/staff/my-permissions")
    public PermissionsResponse getMyPermissions(@AuthenticationPrincipal AppUserPrincipal principal) {
        return permissionService.getMyPermissions(principal.getUser());
    }
}
