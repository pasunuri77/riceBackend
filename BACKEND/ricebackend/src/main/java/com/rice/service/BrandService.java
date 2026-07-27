package com.rice.service;

import com.rice.dto.BrandResponse;
import com.rice.repository.BrandRepository;
import com.rice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public List<BrandResponse> list() {
        return brandRepository.findAll().stream()
                .map(b -> BrandResponse.builder()
                        .id(b.getId())
                        .name(b.getName())
                        .color(b.getColor())
                        .logo(b.getLogo())
                        .products(productRepository.countByBrandId(b.getId()))
                        .build())
                .toList();
    }
}
