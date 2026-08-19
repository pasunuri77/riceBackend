package com.rice.repository;

import com.rice.entity.DeliveryZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {
    List<DeliveryZone> findByActiveTrue();
}
