package com.rice.repository;

import com.rice.entity.ReturnRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRequestItemRepository extends JpaRepository<ReturnRequestItem, Long> {
    
    @Query("SELECT rri FROM ReturnRequestItem rri JOIN rri.returnRequest rr " +
           "WHERE rri.orderItem.id = :orderItemId " +
           "AND rr.status IN ('PENDING', 'APPROVED')")
    List<ReturnRequestItem> findActiveReturnsByOrderItemId(@Param("orderItemId") Long orderItemId);
}
