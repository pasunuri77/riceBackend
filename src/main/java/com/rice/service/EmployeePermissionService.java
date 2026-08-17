package com.rice.service;

import com.rice.dto.staff.PermissionsResponse;
import com.rice.entity.EmployeePermission;
import com.rice.entity.User;
import com.rice.entity.enums.Role;
import com.rice.exception.ApiException;
import com.rice.repository.EmployeePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing employee permissions.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeePermissionService {

    private final EmployeePermissionRepository permissionRepository;

    /**
     * Get permissions for an employee.
     * Returns all-false if employee has no permission record yet.
     */
    public PermissionsResponse getPermissions(Long employeeId) {
        EmployeePermission perm = permissionRepository.findByEmployeeId(employeeId).orElse(null);
        return toResponse(perm);
    }

    /**
     * Get permissions for the current user (must be employee).
     */
    public PermissionsResponse getMyPermissions(User user) {
        if (user.getRole() != Role.EMPLOYEE) {
            // Non-employees get all-false permissions
            return PermissionsResponse.builder().build();
        }
        return getPermissions(user.getId());
    }

    /**
     * Create a blank permission record for a newly promoted/invited employee.
     */
    @Transactional
    public EmployeePermission createBlankPermission(User employee) {
        if (employee.getRole() != Role.EMPLOYEE) {
            throw ApiException.badRequest("User must have employee role to create permissions");
        }

        // Check if permission already exists
        if (permissionRepository.findByEmployeeId(employee.getId()).isPresent()) {
            throw ApiException.conflict("Permission record already exists for this employee");
        }

        EmployeePermission perm = EmployeePermission.builder()
                .employee(employee)
                .canManageProducts(false)
                .canManageOrders(false)
                .canManageCustomers(false)
                .canManageCoupons(false)
                .canViewReports(false)
                .canManageDeliveryTax(false)
                .build();

        return permissionRepository.save(perm);
    }

    /**
     * Update employee permissions.
     */
    @Transactional
    public PermissionsResponse updatePermissions(Long employeeId, PermissionsResponse request) {
        EmployeePermission perm = permissionRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> ApiException.notFound("Permission record not found for employee: " + employeeId));

        perm.setCanManageProducts(request.isCanManageProducts());
        perm.setCanManageOrders(request.isCanManageOrders());
        perm.setCanManageCustomers(request.isCanManageCustomers());
        perm.setCanManageCoupons(request.isCanManageCoupons());
        perm.setCanViewReports(request.isCanViewReports());
        perm.setCanManageDeliveryTax(request.isCanManageDeliveryTax());

        EmployeePermission updated = permissionRepository.save(perm);
        return toResponse(updated);
    }

    private PermissionsResponse toResponse(EmployeePermission perm) {
        if (perm == null) {
            // Return all-false for missing permission records
            return PermissionsResponse.builder().build();
        }
        return PermissionsResponse.builder()
                .canManageProducts(perm.isCanManageProducts())
                .canManageOrders(perm.isCanManageOrders())
                .canManageCustomers(perm.isCanManageCustomers())
                .canManageCoupons(perm.isCanManageCoupons())
                .canViewReports(perm.isCanViewReports())
                .canManageDeliveryTax(perm.isCanManageDeliveryTax())
                .build();
    }
}
