package com.rice.repository;

import com.rice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByCategoryIdAndIdNot(String categoryId, String excludeId);
    long countByCategoryId(String categoryId);
    long countByBrandId(String brandId);
    List<Product> findByBrandName(String brandName);
    List<Product> findByCategoryName(String categoryName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") String id);

    @Query("select p from Product p where p.showInTodaysOffers = true and (p.offerEndDate is null or p.offerEndDate >= :now) order by p.displayPriority asc")
    List<Product> findTodaysOffers(@Param("now") Instant now);

    @Query("select p from Product p where p.showInTodaysOffers = true and p.offerEndDate < :now")
    List<Product> findExpiredOffers(@Param("now") Instant now);
}
