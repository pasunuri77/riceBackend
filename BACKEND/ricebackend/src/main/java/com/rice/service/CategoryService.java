package com.rice.service;

import com.rice.dto.CategoryResponse;
import com.rice.repository.CategoryRepository;
import com.rice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<CategoryResponse> list() {
        return categoryRepository.findAll().stream()
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .icon(c.getIcon())
                        .image(c.getImage())
                        .count(productRepository.countByCategoryId(c.getId()))
                        .build())
                .toList();
    }
}
