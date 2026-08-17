package com.rice.repository;

import com.rice.entity.EmployeePermission;
import com.rice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeePermissionRepository extends JpaRepository<EmployeePermission, Long> {
    Optional<EmployeePermission> findByEmployee(User employee);
    Optional<EmployeePermission> findByEmployeeId(Long employeeId);
}
