package com.rice.controller;

import com.rice.dto.banner.BannerRequest;
import com.rice.dto.banner.BannerResponse;
import com.rice.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping("/banners")
    public List<BannerResponse> listPublic() {
        return bannerService.listPublic();
    }

    @PostMapping("/admin/banners")
    @ResponseStatus(HttpStatus.CREATED)
    public BannerResponse create(@Valid @RequestBody BannerRequest request) {
        return bannerService.create(request);
    }

    @PutMapping("/admin/banners/{id}")
    public BannerResponse update(@PathVariable Long id, @Valid @RequestBody BannerRequest request) {
        return bannerService.update(id, request);
    }

    @DeleteMapping("/admin/banners/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bannerService.delete(id);
    }
}
