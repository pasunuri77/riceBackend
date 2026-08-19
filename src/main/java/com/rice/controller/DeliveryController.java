package com.rice.controller;

import com.rice.dto.delivery.DeliveryAreaResponse;
import com.rice.dto.delivery.ServiceabilityResponse;
import com.rice.dto.delivery.StoreLocationResponse;
import com.rice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/store")
    public ResponseEntity<StoreLocationResponse> getStoreLocation() {
        StoreLocationResponse store = deliveryService.getStoreLocation();
        if (store == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(store);
    }

    @GetMapping("/areas")
    public ResponseEntity<List<DeliveryAreaResponse>> getDeliveryAreas() {
        return ResponseEntity.ok(deliveryService.getDeliveryAreas());
    }

    @GetMapping("/check")
    public ResponseEntity<ServiceabilityResponse> check(@RequestParam(name = "zip", required = false) String zip,
                                                      @RequestParam(name = "pincode", required = false) String pincode,
                                                      @RequestParam(required = false) String productId) {
        String codeToCheck = zip != null ? zip : pincode;
        if (codeToCheck == null) {
            return ResponseEntity.badRequest().body(ServiceabilityResponse.builder().deliverable(false).build());
        }

        if (productId != null && !productId.isBlank()) {
            boolean isProductServiceable = deliveryService.isProductServiceable(productId, codeToCheck);
            if (!isProductServiceable) {
                return ResponseEntity.ok(ServiceabilityResponse.builder()
                        .deliverable(false)
                        .zipCode(codeToCheck)
                        .isNamedZone(false)
                        .build());
            }
        }
        
        return ResponseEntity.ok(deliveryService.checkDelivery(codeToCheck));
    }
}
