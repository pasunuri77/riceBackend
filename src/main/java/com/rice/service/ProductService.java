package com.rice.service;

import com.rice.dto.product.ProductRequest;
import com.rice.dto.product.ProductResponse;
import com.rice.entity.Brand;
import com.rice.entity.Category;
import com.rice.entity.Product;
import com.rice.entity.enums.ProductStatus;
import com.rice.exception.ApiException;
import com.rice.repository.BrandRepository;
import com.rice.repository.CategoryRepository;
import com.rice.repository.ProductRepository;
import com.rice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    public List<ProductResponse> list() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ProductResponse getById(String id) {
        return toResponse(find(id));
    }

    public List<ProductResponse> related(String id, int limit) {
        Product product = find(id);
        String categoryId = product.getCategory() == null ? null : product.getCategory().getId();
        if (categoryId == null) return List.of();
        return productRepository.findByCategoryIdAndIdNot(categoryId, id)
                .stream().limit(limit).map(this::toResponse).toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest req) {
        Product product = Product.builder().id("p" + System.currentTimeMillis()).build();
        apply(product, req);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(String id, ProductRequest req) {
        Product product = find(id);
        apply(product, req);
        return toResponse(product);
    }

    @Transactional
    public void delete(String id) {
        if (!productRepository.existsById(id)) {
            throw ApiException.notFound("Product not found");
        }
        productRepository.deleteById(id);
    }

    private void apply(Product product, ProductRequest req) {
        product.setName(req.getName());
        product.setType(req.getType());
        product.setOrigin(req.getOrigin());
        product.setGrainLength(req.getGrainLength());
        product.setDescription(req.getDescription());
        product.setPricePerKg(req.getPricePerKg());
        product.setMrp(req.getMrp());
        product.setStock(req.getStock());
        product.setMinOrder(req.getMinOrder());
        product.setMaxOrder(req.getMaxOrder());
        product.setImage(req.getImage());
        product.setImages(req.getImages());
        product.setSupplier(req.getSupplier());
        product.setBatchNumber(req.getBatchNumber());
        product.setLotNumber(req.getLotNumber());
        product.setCostPrice(req.getCostPrice());
        product.setNutritionFacts(req.getNutritionFacts());
        product.setCookingInstructions(req.getCookingInstructions());
        product.setAroma(req.getAroma());
        product.setTexture(req.getTexture());
        product.setRiceAge(req.getRiceAge());
        product.setBrokenPercentage(req.getBrokenPercentage());
        product.setShelfLife(req.getShelfLife());
        product.setStorageInstructions(req.getStorageInstructions());
        product.setWeightOptions(req.getWeightOptions());
        product.setBadges(req.getBadges());
        product.setStatus("Inactive".equalsIgnoreCase(req.getStatus()) ? ProductStatus.INACTIVE : ProductStatus.ACTIVE);

        if (req.getBrand() != null) {
            Brand brand = brandRepository.findByName(req.getBrand())
                    .orElseThrow(() -> ApiException.badRequest("Unknown brand: " + req.getBrand()));
            product.setBrand(brand);
        }
        if (req.getCategory() != null) {
            Category category = categoryRepository.findByName(req.getCategory())
                    .orElseThrow(() -> ApiException.badRequest("Unknown category: " + req.getCategory()));
            product.setCategory(category);
        }
    }

    private Product find(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product not found: " + id));
    }

    private ProductResponse toResponse(Product p) {
        long reviewCount = reviewRepository.findByProductId(p.getId()).size();
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .brand(p.getBrand() == null ? null : p.getBrand().getName())
                .brandId(p.getBrand() == null ? null : p.getBrand().getId())
                .category(p.getCategory() == null ? null : p.getCategory().getName())
                .type(p.getType())
                .origin(p.getOrigin())
                .grainLength(p.getGrainLength())
                .description(p.getDescription())
                .pricePerKg(p.getPricePerKg())
                .mrp(p.getMrp())
                .stock(p.getStock())
                .minOrder(p.getMinOrder())
                .maxOrder(p.getMaxOrder())
                .rating(p.getRating())
                .reviews((int) reviewCount)
                .image(p.getImage())
                .images(p.getImages() == null ? List.of() : List.copyOf(p.getImages()))
                .supplier(p.getSupplier())
                .batchNumber(p.getBatchNumber())
                .lotNumber(p.getLotNumber())
                .costPrice(p.getCostPrice())
                .nutritionFacts(p.getNutritionFacts())
                .cookingInstructions(p.getCookingInstructions())
                .aroma(p.getAroma())
                .texture(p.getTexture())
                .riceAge(p.getRiceAge())
                .brokenPercentage(p.getBrokenPercentage())
                .shelfLife(p.getShelfLife())
                .storageInstructions(p.getStorageInstructions())
                .status(p.getStatus() == ProductStatus.ACTIVE ? "Active" : "Inactive")
                // force these lazy element collections to materialize now, while the
                // transaction/session is still open - otherwise Jackson touches them
                // later during response serialization and blows up with LazyInitializationException
                .weightOptions(List.copyOf(p.getWeightOptions()))
                .badges(List.copyOf(p.getBadges()))
                .build();
    }
}
