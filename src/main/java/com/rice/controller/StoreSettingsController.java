package com.rice.controller;

import com.rice.dto.settings.StoreSettingsRequest;
import com.rice.dto.settings.StoreSettingsResponse;
import com.rice.service.StoreSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StoreSettingsController {

    private final StoreSettingsService storeSettingsService;

    @GetMapping({"/api/settings", "/api/admin/settings"})
    public StoreSettingsResponse get() {
        return storeSettingsService.get();
    }

    @PutMapping("/api/admin/settings")
    @PreAuthorize("hasRole('ADMIN') or @permCheck.canManageDeliveryTax(authentication)")
    public StoreSettingsResponse put(@Valid @RequestBody StoreSettingsRequest request) {
        return storeSettingsService.update(request);
    }

    @PatchMapping("/api/admin/settings")
    @PreAuthorize("hasRole('ADMIN') or @permCheck.canManageDeliveryTax(authentication)")
    public StoreSettingsResponse patch(@Valid @RequestBody StoreSettingsRequest request) {
        return storeSettingsService.update(request);
    }
}
