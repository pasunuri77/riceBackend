package com.rice.repository;

import com.rice.entity.User;
import com.rice.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByPhone(String phone);
    List<User> findByRole(Role role);
    
    // Customers (role=USER)
    List<User> findByRoleOrderByCreatedAtDesc(Role role);
    
    // Staff (role=ADMIN or EMPLOYEE)
    @Query("SELECT u FROM User u WHERE u.role IN (com.rice.entity.enums.Role.ADMIN, com.rice.entity.enums.Role.EMPLOYEE) ORDER BY u.createdAt DESC")
    List<User> findAllStaff();
}
