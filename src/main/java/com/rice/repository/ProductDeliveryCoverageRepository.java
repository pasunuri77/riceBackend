package com.rice.repository;

import com.rice.entity.ProductDeliveryCoverage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductDeliveryCoverageRepository extends JpaRepository<ProductDeliveryCoverage, Long> {
    Optional<ProductDeliveryCoverage> findByProductIdAndPincodeAndActiveTrue(String productId, String pincode);
    List<ProductDeliveryCoverage> findByProductIdAndActiveTrue(String productId);
    boolean existsByProductIdAndPincodeAndActiveTrue(String productId, String pincode);
    void deleteByProductIdAndPincode(String productId, String pincode);
}
