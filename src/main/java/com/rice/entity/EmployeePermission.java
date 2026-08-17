package com.rice.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Stores granular permissions for employee-role users.
 * Auto-created (all flags false) whenever a user is invited or promoted to employee.
 */
@Entity
@Table(name = "employee_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private User employee;

    @Column(nullable = false)
    @Builder.Default
    private boolean canManageProducts = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean canManageOrders = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean canManageCustomers = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean canManageCoupons = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean canViewReports = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean canManageDeliveryTax = false;
}
