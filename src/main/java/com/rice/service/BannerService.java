package com.rice.service;

import com.rice.dto.banner.BannerRequest;
import com.rice.dto.banner.BannerResponse;
import com.rice.entity.Banner;
import com.rice.exception.ApiException;
import com.rice.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerRepository bannerRepository;

    public List<BannerResponse> listPublic() {
        return bannerRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .filter(Banner::isActive)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BannerResponse create(BannerRequest request) {
        Banner banner = Banner.builder()
                .title(request.getTitle().trim())
                .subtitle(request.getSubtitle())
                .imageUrl(request.getImageUrl())
                .linkTo(request.getLinkTo())
                .active(request.getActive() != null ? request.getActive() : true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();
        return toResponse(bannerRepository.save(banner));
    }

    @Transactional
    public BannerResponse update(Long id, BannerRequest request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Banner not found: " + id));

        banner.setTitle(request.getTitle().trim());
        banner.setSubtitle(request.getSubtitle());
        banner.setImageUrl(request.getImageUrl());
        banner.setLinkTo(request.getLinkTo());
        banner.setActive(request.getActive() != null ? request.getActive() : banner.isActive());
        banner.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : banner.getSortOrder());

        return toResponse(bannerRepository.save(banner));
    }

    @Transactional
    public void delete(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Banner not found: " + id));
        bannerRepository.delete(banner);
    }

    private BannerResponse toResponse(Banner banner) {
        return BannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .subtitle(banner.getSubtitle())
                .imageUrl(banner.getImageUrl())
                .linkTo(banner.getLinkTo())
                .active(banner.isActive())
                .sortOrder(banner.getSortOrder())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }
}
