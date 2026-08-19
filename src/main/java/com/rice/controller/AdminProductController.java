package com.rice.controller;

import com.rice.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AdminProductController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/api/admin/products/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or @permCheck.canManageProducts(authentication)")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = cloudinaryService.upload(file);
        return Map.of("url", url);
    }
}
