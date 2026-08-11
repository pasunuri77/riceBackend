package com.rice.service;

import com.rice.repository.ServiceablePincodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final ServiceablePincodeRepository repo;

    public boolean isServiceable(String pincode) {
        if (pincode == null) return false;
        String normalized = pincode.trim();
        if (!normalized.matches("\\\\d{6}")) return false;
        return repo.findByPincode(normalized).isPresent();
    }

    public java.util.List<String> listPincodes() {
        return repo.findAll().stream().map(p -> p.getPincode()).toList();
    }

    public java.util.List<String> addPincodes(java.util.List<String> pincodes) {
        if (pincodes == null || pincodes.isEmpty()) return java.util.List.of();
        java.util.List<com.rice.entity.ServiceablePincode> toSave = new java.util.ArrayList<>();
        for (String p : pincodes) {
            if (p == null) continue;
            String n = p.trim();
            if (!n.matches("\\\\d{6}")) continue;
            boolean exists = repo.findByPincode(n).isPresent();
            if (!exists) {
                toSave.add(com.rice.entity.ServiceablePincode.builder().pincode(n).build());
            }
        }
        if (toSave.isEmpty()) return java.util.List.of();
        return repo.saveAll(toSave).stream().map(s -> s.getPincode()).toList();
    }

    public void removePincode(String pincode) {
        if (pincode == null) return;
        String n = pincode.trim();
        repo.findByPincode(n).ifPresent(repo::delete);
    }
}
