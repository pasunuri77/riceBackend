package com.rice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rice.dto.settings.StoreSettingsRequest;
import com.rice.dto.settings.StoreSettingsResponse;
import com.rice.entity.StoreSettings;
import com.rice.exception.ApiException;
import com.rice.repository.StoreSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreSettingsService {

    private static final long SETTINGS_ID = 1L;

    private final StoreSettingsRepository storeSettingsRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${store.delivery-charge:49}")
    private BigDecimal defaultDeliveryCharge;

    @Value("${store.free-delivery-threshold:999}")
    private BigDecimal defaultFreeDeliveryThreshold;

    public StoreSettingsResponse get() {
        return toResponse(getOrCreate());
    }

    StoreSettings current() {
        return getOrCreate();
    }

    @Transactional
    public StoreSettingsResponse update(StoreSettingsRequest request) {
        StoreSettings settings = getOrCreate();
        settings.setStoreName(request.getStoreName());
        settings.setGstNumber(request.getGstNumber());
        settings.setPhone(request.getPhone());
        settings.setLogo(request.getLogo());
        settings.setEmail(request.getEmail());
        settings.setCurrency(blankToDefault(request.getCurrency(), "INR"));
        settings.setDeliveryCharge(defaultMoney(request.getDeliveryCharge()));
        settings.setFreeDeliveryThreshold(defaultMoney(request.getFreeDeliveryThreshold()));
        settings.setTaxPercentage(defaultMoney(request.getTaxPercentage()));
        settings.setBusinessHours(writeHours(request.getBusinessHours()));
        return toResponse(storeSettingsRepository.save(settings));
    }

    private StoreSettings getOrCreate() {
        return storeSettingsRepository.findById(SETTINGS_ID)
                .orElseGet(() -> storeSettingsRepository.save(defaultSettings()));
    }

    private StoreSettings defaultSettings() {
        return StoreSettings.builder()
                .id(SETTINGS_ID)
                .storeName("RiceBazaar")
                .gstNumber("")
                .phone("")
                .logo("")
                .email("")
                .currency("INR")
                .deliveryCharge(defaultMoney(defaultDeliveryCharge))
                .freeDeliveryThreshold(defaultMoney(defaultFreeDeliveryThreshold))
                .taxPercentage(BigDecimal.ZERO)
                .businessHours(writeHours(defaultHours()))
                .build();
    }

    private StoreSettingsResponse toResponse(StoreSettings settings) {
        return StoreSettingsResponse.builder()
                .storeName(settings.getStoreName())
                .gstNumber(settings.getGstNumber())
                .phone(settings.getPhone())
                .logo(settings.getLogo())
                .email(settings.getEmail())
                .currency(settings.getCurrency())
                .deliveryCharge(settings.getDeliveryCharge())
                .freeDeliveryThreshold(settings.getFreeDeliveryThreshold())
                .taxPercentage(settings.getTaxPercentage())
                .businessHours(readHours(settings.getBusinessHours()))
                .build();
    }

    private Map<String, StoreSettingsRequest.BusinessHourRequest> defaultHours() {
        Map<String, StoreSettingsRequest.BusinessHourRequest> hours = new LinkedHashMap<>();
        for (String day : new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}) {
            StoreSettingsRequest.BusinessHourRequest hour = new StoreSettingsRequest.BusinessHourRequest();
            hour.setOpen("09:00");
            hour.setClose("18:00");
            hour.setClosed(false);
            hours.put(day, hour);
        }
        return hours;
    }

    private Map<String, StoreSettingsRequest.BusinessHourRequest> readHours(String json) {
        if (json == null || json.isBlank()) {
            return defaultHours();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            throw ApiException.badRequest("Invalid business hours data");
        }
    }

    private String writeHours(Map<String, StoreSettingsRequest.BusinessHourRequest> hours) {
        try {
            return objectMapper.writeValueAsString(hours == null ? defaultHours() : hours);
        } catch (Exception ex) {
            throw ApiException.badRequest("Invalid business hours data");
        }
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
