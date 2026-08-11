package com.rice.repository;

import com.rice.entity.ProductEvent;
import com.rice.entity.enums.ProductEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ProductEventRepository extends JpaRepository<ProductEvent, Long> {
    long countByProductIdAndType(String productId, ProductEventType type);

    @Query("select p.type, count(p) from ProductEvent p where p.productId = :productId group by p.type")
    List<Object[]> countByProductGrouped(@Param("productId") String productId);

    @Query("select count(p) from ProductEvent p where p.productId = :productId and p.type = :type and p.createdAt >= :since")
    long countByProductAndTypeSince(@Param("productId") String productId, @Param("type") ProductEventType type, @Param("since") Instant since);
}
