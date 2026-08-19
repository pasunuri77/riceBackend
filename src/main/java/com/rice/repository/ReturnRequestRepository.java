package com.rice.repository;

import com.rice.entity.ReturnRequest;
import com.rice.entity.enums.ReturnRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    List<ReturnRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<ReturnRequest> findByStatusOrderByCreatedAtDesc(ReturnRequestStatus status);
    Optional<ReturnRequest> findByIdAndCustomerId(Long id, Long customerId);
}
