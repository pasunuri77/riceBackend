package com.rice.security;

import com.rice.entity.EmployeePermission;
import com.rice.entity.User;
import com.rice.entity.enums.Role;
import com.rice.repository.EmployeePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Permission check bean for granular authorization of employee-role users.
 * Used with @PreAuthorize("hasRole('ADMIN') or @permCheck.canManageOrders(authentication)")
 * 
 * Admin role bypasses all checks. Employee role is checked against EmployeePermission flags.
 */
@Component("permCheck")
@RequiredArgsConstructor
public class PermissionCheck {

    private final EmployeePermissionRepository permissionRepository;

    public boolean canManageProducts(Authentication authentication) {
        return checkPermission(authentication, "canManageProducts");
    }

    public boolean canManageOrders(Authentication authentication) {
        return checkPermission(authentication, "canManageOrders");
    }

    public boolean canManageCustomers(Authentication authentication) {
        return checkPermission(authentication, "canManageCustomers");
    }

    public boolean canManageCoupons(Authentication authentication) {
        return checkPermission(authentication, "canManageCoupons");
    }

    public boolean canViewReports(Authentication authentication) {
        return checkPermission(authentication, "canViewReports");
    }

    public boolean canManageDeliveryTax(Authentication authentication) {
        return checkPermission(authentication, "canManageDeliveryTax");
    }

    private boolean checkPermission(Authentication authentication, String permission) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal)) {
            return false;
        }

        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        // Admin always has all permissions
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // Only employees can have granular permissions
        if (user.getRole() != Role.EMPLOYEE) {
            return false;
        }

        // Check employee permission flag
        EmployeePermission perm = permissionRepository.findByEmployeeId(user.getId()).orElse(null);
        if (perm == null) {
            return false;
        }

        return switch (permission) {
            case "canManageProducts" -> perm.isCanManageProducts();
            case "canManageOrders" -> perm.isCanManageOrders();
            case "canManageCustomers" -> perm.isCanManageCustomers();
            case "canManageCoupons" -> perm.isCanManageCoupons();
            case "canViewReports" -> perm.isCanViewReports();
            case "canManageDeliveryTax" -> perm.isCanManageDeliveryTax();
            default -> false;
        };
    }
}
