package com.rice.controller;


import com.rice.dto.product.ProductRequest;
import com.rice.dto.product.ProductResponse;
import com.rice.dto.product.UpdateProductOfferRequest;
import com.rice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductResponse> list() {

        return productService.list();
    }

    @GetMapping("/todays-offers")
    public List<ProductResponse> todaysOffers() {
        return productService.todaysOffers();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable String id) {
        return productService.getById(id);
    }

    @GetMapping("/{id}/related")
    public List<ProductResponse> related(@PathVariable String id, @RequestParam(defaultValue = "4") int limit) {
        return productService.related(id, limit);
    }

    @PostMapping
    public ProductResponse create(@RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable String id, @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PatchMapping("/admin/products/{id}/offer")
    public ProductResponse updateOffer(@PathVariable String id, @RequestBody UpdateProductOfferRequest request) {
        return productService.updateOffer(id, request);
    }

    @PatchMapping("/admin/products/reorder")
    public void reorderOffers(@RequestBody List<String> orderedIds) {
        productService.reorderOffers(orderedIds);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        productService.delete(id);
    }
}
