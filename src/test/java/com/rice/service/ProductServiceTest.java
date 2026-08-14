package com.rice.service;

import com.rice.exception.ApiException;
import com.rice.repository.BrandRepository;
import com.rice.repository.CategoryRepository;
import com.rice.repository.OrderItemRepository;
import com.rice.repository.ProductRepository;
import com.rice.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private OrderItemRepository orderItemRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, brandRepository, categoryRepository, reviewRepository, orderItemRepository);
    }

    @Test
    void delete_throwsBadRequest_whenProductHasExistingOrders() {
        when(productRepository.existsById("p123")).thenReturn(true);
        when(orderItemRepository.existsByProductId("p123")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> productService.delete("p123"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("existing orders"));
        verify(productRepository, never()).deleteById(anyString());
    }

    @Test
    void delete_throwsBadRequest_whenProductHasExistingReviews() {
        when(productRepository.existsById("p123")).thenReturn(true);
        when(orderItemRepository.existsByProductId("p123")).thenReturn(false);
        when(reviewRepository.existsByProductId("p123")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> productService.delete("p123"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("existing reviews"));
        verify(productRepository, never()).deleteById(anyString());
    }
}
