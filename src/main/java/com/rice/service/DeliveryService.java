package com.rice.service;

import com.rice.dto.delivery.DeliveryAreaResponse;
import com.rice.dto.delivery.ServiceabilityResponse;
import com.rice.dto.delivery.StoreLocationResponse;
import com.rice.entity.DeliveryZone;
import com.rice.entity.ProductDeliveryCoverage;
import com.rice.entity.ServiceablePincode;
import com.rice.entity.StoreLocation;
import com.rice.repository.DeliveryZoneRepository;
import com.rice.repository.ProductDeliveryCoverageRepository;
import com.rice.repository.ProductRepository;
import com.rice.repository.ServiceablePincodeRepository;
import com.rice.repository.StoreLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DeliveryService {
    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);
    private static final Pattern ZIP_PATTERN = Pattern.compile("\\d{5}(?:-\\d{4})?");

    private final ServiceablePincodeRepository repo;
    private final ProductRepository productRepository;
    private final ProductDeliveryCoverageRepository productDeliveryCoverageRepository;
    private final StoreLocationRepository storeLocationRepository;
    private final DeliveryZoneRepository deliveryZoneRepository;

    @Autowired
    public DeliveryService(ServiceablePincodeRepository repo,
                          ProductRepository productRepository,
                          ProductDeliveryCoverageRepository productDeliveryCoverageRepository,
                          StoreLocationRepository storeLocationRepository,
                          DeliveryZoneRepository deliveryZoneRepository) {
        this.repo = repo;
        this.productRepository = productRepository;
        this.productDeliveryCoverageRepository = productDeliveryCoverageRepository;
        this.storeLocationRepository = storeLocationRepository;
        this.deliveryZoneRepository = deliveryZoneRepository;
    }

    public DeliveryService(ServiceablePincodeRepository repo, ProductRepository productRepository) {
        this(repo, productRepository, null, null, null);
    }

    public DeliveryService(ServiceablePincodeRepository repo) {
        this(repo, null, null, null, null);
    }

    public StoreLocationResponse getStoreLocation() {
        if (storeLocationRepository == null) return null;
        List<StoreLocation> activeLocations = storeLocationRepository.findByActiveTrue();
        if (activeLocations.isEmpty()) {
            return null;
        }
        StoreLocation loc = activeLocations.get(0);
        return StoreLocationResponse.builder()
                .name(loc.getName())
                .address(loc.getAddress())
                .area(loc.getArea())
                .city(loc.getCity())
                .state(loc.getState())
                .stateCode(loc.getStateCode())
                .zip(loc.getZip())
                .country(loc.getCountry())
                .countryCode(loc.getCountryCode())
                .build();
    }

    public List<DeliveryAreaResponse> getDeliveryAreas() {
        List<DeliveryAreaResponse> responses = new ArrayList<>();
        if (deliveryZoneRepository == null) return responses;
        
        List<DeliveryZone> activeZones = deliveryZoneRepository.findByActiveTrue();
        List<ServiceablePincode> allActivePincodes = repo.findAll().stream()
                .filter(ServiceablePincode::isActive)
                .toList();

        Map<Long, List<String>> zoneToZips = new HashMap<>();
        List<String> greaterAustinZips = new ArrayList<>();

        for (ServiceablePincode sp : allActivePincodes) {
            if (sp.getZone() != null) {
                zoneToZips.computeIfAbsent(sp.getZone().getId(), k -> new ArrayList<>()).add(sp.getPincode());
            } else if (!sp.isNamedZone()) {
                greaterAustinZips.add(sp.getPincode());
            }
        }

        for (DeliveryZone zone : activeZones) {
            responses.add(DeliveryAreaResponse.builder()
                    .id(zone.getId())
                    .name(zone.getName())
                    .description(zone.getDescription())
                    .zipCodes(zoneToZips.getOrDefault(zone.getId(), new ArrayList<>()))
                    .isNamedZone(true)
                    .build());
        }

        if (!greaterAustinZips.isEmpty()) {
            responses.add(DeliveryAreaResponse.builder()
                    .id(999L)
                    .name("Greater Austin")
                    .description(null)
                    .zipCodes(greaterAustinZips)
                    .isNamedZone(false)
                    .build());
        }

        return responses;
    }

    public ServiceabilityResponse checkDelivery(String pincode) {
        if (pincode == null) {
            return ServiceabilityResponse.builder().deliverable(false).isNamedZone(false).build();
        }
        
        String normalized = pincode.trim();
        if (normalized.length() > 5) {
            normalized = normalized.substring(0, 5);
        }

        boolean matches = ZIP_PATTERN.matcher(normalized).matches();
        if (!matches) {
            log.debug("checkDelivery: pincode '{}' rejected by format check", normalized);
            return ServiceabilityResponse.builder().deliverable(false).zipCode(normalized).isNamedZone(false).build();
        }

        Optional<ServiceablePincode> opt = repo.findByPincode(normalized);
        if (opt.isPresent() && opt.get().isActive()) {
            ServiceablePincode sp = opt.get();
            String areaName = sp.getZone() != null ? sp.getZone().getName() : (sp.isNamedZone() ? "Named Zone" : "Greater Austin");
            return ServiceabilityResponse.builder()
                    .deliverable(true)
                    .zipCode(normalized)
                    .areaName(areaName)
                    .isNamedZone(sp.isNamedZone())
                    .build();
        }

        return ServiceabilityResponse.builder()
                .deliverable(false)
                .zipCode(normalized)
                .isNamedZone(false)
                .build();
    }

    public boolean isServiceable(String pincode) {
        return checkDelivery(pincode).isDeliverable();
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
        boolean result = productDeliveryCoverageRepository == null
                || productDeliveryCoverageRepository.findByProductIdAndPincodeAndActiveTrue(normalizedProductId, pincode.trim()).isPresent()
                || productDeliveryCoverageRepository.findByProductIdAndActiveTrue(normalizedProductId).isEmpty();
        log.debug("isProductServiceable: productId='{}' pincode='{}' -> {}", normalizedProductId, pincode, result);
        return result;
    }

    public List<String> listPincodes() {
        return repo.findAll().stream().map(p -> p.getPincode()).toList();
    }

    public List<String> addPincodes(List<String> pincodes) {
        if (pincodes == null || pincodes.isEmpty()) return List.of();
        List<ServiceablePincode> toSave = new ArrayList<>();
        for (String p : pincodes) {
            if (p == null) continue;
            String n = p.trim();
            if (!ZIP_PATTERN.matcher(n).matches()) continue;
            boolean exists = repo.findByPincode(n).isPresent();
            if (!exists) {
                toSave.add(ServiceablePincode.builder().pincode(n).active(true).isNamedZone(false).build());
            }
        }
        if (toSave.isEmpty()) return List.of();
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
            if (!ZIP_PATTERN.matcher(n).matches()) continue;
            if (!repo.findByPincode(n).isPresent()) {
                repo.save(new ServiceablePincode(null, n));
            }
            if (productDeliveryCoverageRepository != null && !productDeliveryCoverageRepository.existsByProductIdAndPincodeAndActiveTrue(productId.trim(), n)) {
                productDeliveryCoverageRepository.save(ProductDeliveryCoverage.builder()
                        .productId(productId.trim())
                        .pincode(n)
                        .active(true)
                        .build());
            }
        }

        if (productDeliveryCoverageRepository != null) {
            return productDeliveryCoverageRepository.findByProductIdAndActiveTrue(productId.trim())
                    .stream().map(ProductDeliveryCoverage::getPincode).toList();
        }
        return List.of();
    }

    public List<String> listProductPincodes(String productId) {
        if (productId == null || productId.isBlank()) return List.of();
        if (productDeliveryCoverageRepository != null) {
            return productDeliveryCoverageRepository.findByProductIdAndActiveTrue(productId.trim())
                    .stream().map(ProductDeliveryCoverage::getPincode).toList();
        }
        return List.of();
    }

    public void removeProductPincode(String productId, String pincode) {
        if (productId == null || productId.isBlank() || pincode == null || pincode.isBlank()) return;
        if (productDeliveryCoverageRepository != null) {
            productDeliveryCoverageRepository.deleteByProductIdAndPincode(productId.trim(), pincode.trim());
        }
    }
}
