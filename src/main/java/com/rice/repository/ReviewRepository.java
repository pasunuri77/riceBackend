package com.rice.repository;

import com.rice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(String productId);
    boolean existsByProductId(String productId);
}
