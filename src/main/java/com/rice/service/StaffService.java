package com.rice.service;

import com.rice.dto.staff.StaffResponse;
import com.rice.dto.staff.StaffInviteRequest;
import com.rice.entity.EmployeePermission;
import com.rice.entity.PasswordResetToken;
import com.rice.entity.User;
import com.rice.entity.enums.Role;
import com.rice.entity.enums.UserStatus;
import com.rice.exception.ApiException;
import com.rice.repository.EmployeePermissionRepository;
import com.rice.repository.PasswordResetTokenRepository;
import com.rice.repository.UserRepository;
import com.rice.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing staff (admin/employee) users.
 * Enforces role hierarchy:
 * - Nobody can delete their own account
 * - admin can delete any account
 * - employee can only delete user-role (customer) accounts
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffService {

    private final UserRepository userRepository;
    private final EmployeePermissionRepository permissionRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmployeePermissionService employeePermissionService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    /**
     * List all staff (admin and employee roles).
     */
    public List<StaffResponse> listAll() {
        return userRepository.findAllStaff().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get a staff member by ID.
     */
    public StaffResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Staff member not found: " + id));

        if (user.getRole() == Role.USER) {
            throw ApiException.notFound("Staff member not found: " + id);
        }

        return toResponse(user);
    }

    /**
     * Invite a new staff member (admin or employee).
     * Generates a password-reset token and sends invitation email.
     */
    @Transactional
    public StaffResponse invite(StaffInviteRequest request) {
        // Validate role
        Role role = parseRole(request.getRole());
        if (role == Role.USER) {
            throw ApiException.badRequest("Invalid role for staff invitation: " + request.getRole());
        }

        // Check email doesn't exist
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("Email already registered: " + request.getEmail());
        }

        // Create user with random temp password
        String tempPassword = UUID.randomUUID().toString();
        User user = User.builder()
                .name(request.getFullName())
                .email(request.getEmail())
                .phone(request.getMobile())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);

        // If employee role, create blank permission record
        if (role == Role.EMPLOYEE) {
            employeePermissionService.createBlankPermission(saved);
        }

        // Generate password-reset token for 24h
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(saved)
                .token(token)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        tokenRepository.save(resetToken);

        // Send invitation email
        String resetLink = frontendBaseUrl + "/set-password?token=" + token;
        emailService.sendInvitationEmail(saved.getEmail(), saved.getName(), resetLink, role.name().toLowerCase());

        return toResponse(saved);
    }

    /**
     * Change a staff member's role.
     * If promoting to employee and no permission record exists, create blank one.
     */
    @Transactional
    public StaffResponse changeRole(Long id, String newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Staff member not found: " + id));

        if (user.getRole() == Role.USER) {
            throw ApiException.notFound("Staff member not found: " + id);
        }

        Role role = parseRole(newRole);
        if (role == Role.USER) {
            throw ApiException.badRequest("Invalid role: " + newRole);
        }

        user.setRole(role);
        User updated = userRepository.save(user);

        // If promoting to employee, ensure permission record exists
        if (role == Role.EMPLOYEE && permissionRepository.findByEmployeeId(id).isEmpty()) {
            employeePermissionService.createBlankPermission(updated);
        }

        return toResponse(updated);
    }

    /**
     * Change a staff member's status (ACTIVE/INACTIVE).
     */
    @Transactional
    public StaffResponse changeStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Staff member not found: " + id));

        if (user.getRole() == Role.USER) {
            throw ApiException.notFound("Staff member not found: " + id);
        }

        UserStatus userStatus = parseStatus(status);
        user.setStatus(userStatus);
        User updated = userRepository.save(user);

        return toResponse(updated);
    }

    /**
     * Delete a staff or customer account.
     * Enforces role hierarchy:
     * - Nobody can delete their own account (403 or 409)
     * - admin can delete any account (admin, employee, or customer)
     * - employee can only delete user-role accounts (customers), reject with 403 for other staff
     */
    @Transactional
    public void delete(Long targetId, User caller) {
        // Check: caller cannot delete their own account
        if (caller.getId().equals(targetId)) {
            throw ApiException.conflict("Cannot delete your own account");
        }

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> ApiException.notFound("User not found: " + targetId));

        // Admin can delete any account
        if (caller.getRole() == Role.ADMIN) {
            userRepository.delete(target);
            return;
        }

        // Employee can only delete customer (user-role) accounts
        if (caller.getRole() == Role.EMPLOYEE) {
            if (target.getRole() != Role.USER) {
                throw ApiException.forbidden("Employees can only delete customer accounts");
            }
            userRepository.delete(target);
            return;
        }

        // Regular user (USER role) cannot delete anyone
        throw ApiException.forbidden("You do not have permission to delete accounts");
    }

    /**
     * Delete a staff account via dedicated endpoint (checks role hierarchy).
     */
    @Transactional
    public void deleteStaffAccount(Long targetId, User caller) {
        // Check: caller cannot delete their own account
        if (caller.getId().equals(targetId)) {
            throw ApiException.conflict("Cannot delete your own account");
        }

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> ApiException.notFound("Staff member not found: " + targetId));

        if (target.getRole() == Role.USER) {
            throw ApiException.notFound("Staff member not found: " + targetId);
        }

        // Admin can delete any staff (another admin or employee)
        if (caller.getRole() == Role.ADMIN) {
            userRepository.delete(target);
            return;
        }

        // Employee cannot delete other admins or employees
        if (caller.getRole() == Role.EMPLOYEE) {
            throw ApiException.forbidden("Employees cannot delete other staff members");
        }

        // Regular user cannot delete staff
        throw ApiException.forbidden("You do not have permission to delete staff accounts");
    }

    private StaffResponse toResponse(User user) {
        return StaffResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getPhone())
                .role(user.getRole().name().toLowerCase())
                .status(user.getStatus().name())
                .joined(DateTimeFormatter.ISO_LOCAL_DATE.format(user.getCreatedAt().atZone(ZoneOffset.UTC)))
                .pendingSetup(false)  // Could check if password is still temp, but keeping simple
                .build();
    }

    private Role parseRole(String role) {
        if (role == null) {
            throw ApiException.badRequest("Role is required");
        }
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Invalid role: " + role);
        }
    }

    private UserStatus parseStatus(String status) {
        if (status == null) {
            throw ApiException.badRequest("Status is required");
        }
        try {
            return UserStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Invalid status: " + status);
        }
    }
}
