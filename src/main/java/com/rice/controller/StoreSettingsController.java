package com.rice.controller;

import com.rice.dto.settings.StoreSettingsRequest;
import com.rice.dto.settings.StoreSettingsResponse;
import com.rice.service.StoreSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class StoreSettingsController {

    private final StoreSettingsService storeSettingsService;

    @GetMapping
    public StoreSettingsResponse get() {
        return storeSettingsService.get();
    }

    @PutMapping
    public StoreSettingsResponse put(@Valid @RequestBody StoreSettingsRequest request) {
        return storeSettingsService.update(request);
    }

    @PatchMapping
    public StoreSettingsResponse patch(@Valid @RequestBody StoreSettingsRequest request) {
        return storeSettingsService.update(request);
    }
}
