package com.rice.repository;

import com.rice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByCategoryIdAndIdNot(String categoryId, String excludeId);
    long countByCategoryId(String categoryId);
    long countByBrandId(String brandId);
    List<Product> findByBrandName(String brandName);
    List<Product> findByCategoryName(String categoryName);
}
