package com.rice.controller;

import com.rice.dto.delivery.ServiceabilityResponse;
import com.rice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/check")
    public ServiceabilityResponse check(@RequestParam String pincode,
                                      @RequestParam(required = false) String productId) {
        boolean ok = (productId == null || productId.isBlank())
                ? deliveryService.isServiceable(pincode)
                : deliveryService.isProductServiceable(productId, pincode);
        return ServiceabilityResponse.builder().serviceable(ok).pincode(pincode).build();
    }
}
