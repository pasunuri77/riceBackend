package com.rice.service;

import com.rice.dto.banner.BannerResponse;
import com.rice.entity.Banner;
import com.rice.repository.BannerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BannerServiceTest {

    @Mock
    private BannerRepository bannerRepository;

    @InjectMocks
    private BannerService bannerService;

    @Test
    void listPublicReturnsOnlyActiveBannersOrderedBySortOrder() {
        Banner first = Banner.builder()
                .id(1L)
                .title("Summer Sale")
                .active(true)
                .sortOrder(1)
                .build();
        Banner second = Banner.builder()
                .id(2L)
                .title("New Arrivals")
                .active(true)
                .sortOrder(2)
                .build();
        Banner hidden = Banner.builder()
                .id(3L)
                .title("Hidden Promo")
                .active(false)
                .sortOrder(0)
                .build();

        when(bannerRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(first, second, hidden));

        List<BannerResponse> result = bannerService.listPublic();

        assertEquals(2, result.size());
        assertEquals("Summer Sale", result.get(0).getTitle());
        assertEquals("New Arrivals", result.get(1).getTitle());
    }
}
