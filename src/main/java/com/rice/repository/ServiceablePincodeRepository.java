package com.rice.repository;

import com.rice.entity.ServiceablePincode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceablePincodeRepository extends JpaRepository<ServiceablePincode, Long> {
    Optional<ServiceablePincode> findByPincode(String pincode);
}
