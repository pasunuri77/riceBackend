package com.rice.service;

import com.rice.entity.ProductDeliveryCoverage;
import com.rice.repository.ProductDeliveryCoverageRepository;
import com.rice.repository.ProductRepository;
import com.rice.repository.ServiceablePincodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryService {

    private final ServiceablePincodeRepository repo;
    private final ProductRepository productRepository;
    private final ProductDeliveryCoverageRepository productDeliveryCoverageRepository;

    @Autowired
    public DeliveryService(ServiceablePincodeRepository repo,
                          ProductRepository productRepository,
                          ProductDeliveryCoverageRepository productDeliveryCoverageRepository) {
        this.repo = repo;
        this.productRepository = productRepository;
        this.productDeliveryCoverageRepository = productDeliveryCoverageRepository;
    }

    public DeliveryService(ServiceablePincodeRepository repo, ProductRepository productRepository) {
        this(repo, productRepository, null);
    }

    public DeliveryService(ServiceablePincodeRepository repo) {
        this(repo, null, null);
    }

    public boolean isServiceable(String pincode) {
        if (pincode == null) return false;
        String normalized = pincode.trim();
        if (!normalized.matches("\\d{6}")) return false;
        return repo.findByPincode(normalized).isPresent();
    }

    public boolean isProductServiceable(String productId, String pincode) {
        if (productId == null || productId.isBlank()) return false;
        String normalizedProductId = productId.trim();
        if (productRepository != null && !productRepository.existsById(normalizedProductId)) {
            return false;
        }
        if (!isServiceable(pincode)) {
            return false;
        }
        return productDeliveryCoverageRepository == null
                || productDeliveryCoverageRepository.findByProductIdAndPincodeAndActiveTrue(normalizedProductId, pincode.trim()).isPresent()
                || productDeliveryCoverageRepository.findByProductIdAndActiveTrue(normalizedProductId).isEmpty();
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
            if (!n.matches("\\d{6}")) continue;
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

    public List<String> addProductPincodes(String productId, List<String> pincodes) {
        if (productId == null || productId.isBlank()) return List.of();
        if (productRepository != null && !productRepository.existsById(productId.trim())) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        if (pincodes == null || pincodes.isEmpty()) return List.of();

        for (String p : pincodes) {
            if (p == null) continue;
            String n = p.trim();
            if (!n.matches("\\d{6}")) continue;
            if (!repo.findByPincode(n).isPresent()) {
                repo.save(new com.rice.entity.ServiceablePincode(null, n));
            }
            if (!productDeliveryCoverageRepository.existsByProductIdAndPincodeAndActiveTrue(productId.trim(), n)) {
                productDeliveryCoverageRepository.save(ProductDeliveryCoverage.builder()
                        .productId(productId.trim())
                        .pincode(n)
                        .active(true)
                        .build());
            }
        }

        return productDeliveryCoverageRepository.findByProductIdAndActiveTrue(productId.trim())
                .stream().map(ProductDeliveryCoverage::getPincode).toList();
    }

    public List<String> listProductPincodes(String productId) {
        if (productId == null || productId.isBlank()) return List.of();
        return productDeliveryCoverageRepository.findByProductIdAndActiveTrue(productId.trim())
                .stream().map(ProductDeliveryCoverage::getPincode).toList();
    }

    public void removeProductPincode(String productId, String pincode) {
        if (productId == null || productId.isBlank() || pincode == null || pincode.isBlank()) return;
        productDeliveryCoverageRepository.deleteByProductIdAndPincode(productId.trim(), pincode.trim());
    }
}
