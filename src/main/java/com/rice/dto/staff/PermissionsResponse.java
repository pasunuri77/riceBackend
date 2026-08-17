package com.rice.dto.staff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response for GET /api/admin/staff/{id}/permissions and GET /api/staff/my-permissions
 * Also used for PUT /api/admin/staff/{id}/permissions
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionsResponse {
    private boolean canManageProducts;
    private boolean canManageOrders;
    private boolean canManageCustomers;
    private boolean canManageCoupons;
    private boolean canViewReports;
    private boolean canManageDeliveryTax;
}
