package com.rice.repository;

import com.rice.entity.Order;
import com.rice.entity.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Order> findAllByOrderByCreatedAtDesc();
    long countByCustomerId(Long customerId);
    long countByDeliveryStatus(DeliveryStatus status);
    boolean existsByCustomerIdAndItemsProductId(Long customerId, String productId);

    @Query("select coalesce(sum(o.amount), 0) from Order o where o.customer.id = :customerId")
    BigDecimal totalSpentByCustomer(@Param("customerId") Long customerId);

    @Query("select coalesce(sum(o.amount), 0) from Order o")
    BigDecimal totalRevenue();
}
