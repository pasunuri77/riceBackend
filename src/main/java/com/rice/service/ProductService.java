package com.rice.service;

import com.rice.dto.product.ProductRequest;
import com.rice.dto.product.ProductResponse;
import com.rice.dto.product.UpdateProductOfferRequest;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    public List<ProductResponse> todaysOffers() {
        return productRepository.findTodaysOffers(Instant.now()).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public ProductResponse updateOffer(String id, UpdateProductOfferRequest request) {
        Product product = find(id);
        product.setShowInTodaysOffers(Boolean.TRUE.equals(request.getShowInTodaysOffers()));
        product.setDisplayPriority(request.getDisplayPriority() == null ? 0 : request.getDisplayPriority());
        product.setOfferEndDate(request.getOfferEndDate());
        product.setLowStockThreshold(request.getLowStockThreshold());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void reorderOffers(List<String> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw ApiException.badRequest("Ordered product list cannot be empty");
        }
        List<Product> products = productRepository.findAllById(orderedIds);
        if (products.size() != orderedIds.size()) {
            throw ApiException.badRequest("Some product ids are invalid");
        }
        for (int i = 0; i < orderedIds.size(); i++) {
            String productId = orderedIds.get(i);
            Product product = products.stream()
                    .filter(p -> p.getId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> ApiException.badRequest("Unknown product id: " + productId));
            product.setDisplayPriority(i);
        }
        productRepository.saveAll(products);
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void expireTodaysOffers() {
        List<Product> expired = productRepository.findExpiredOffers(Instant.now());
        if (expired.isEmpty()) {
            return;
        }
        expired.forEach(p -> p.setShowInTodaysOffers(false));
        productRepository.saveAll(expired);
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
        product.setShowInTodaysOffers(Boolean.TRUE.equals(req.getShowInTodaysOffers()));
        product.setDisplayPriority(req.getDisplayPriority() == null ? 0 : req.getDisplayPriority());
        product.setOfferEndDate(req.getOfferEndDate());
        product.setLowStockThreshold(req.getLowStockThreshold());
        product.setMrp(req.getMrp());
        product.setStock(computeTotalStock(req));
        product.setStock1Kg(req.getStock1Kg());
        product.setStock5Kg(req.getStock5Kg());
        product.setStock10Kg(req.getStock10Kg());
        product.setStock50Kg(req.getStock50Kg());
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
            Brand brand = brandRepository.findByName(req.getBrand()).orElseGet(() -> {
                String id = slugify(req.getBrand());
                if (brandRepository.existsById(id)) {
                    id = id + "-" + System.currentTimeMillis();
                }
                Brand b = Brand.builder().id(id).name(req.getBrand()).build();
                return brandRepository.save(b);
            });
            product.setBrand(brand);
        }
        if (req.getCategory() != null) {
            Category category = categoryRepository.findByName(req.getCategory()).orElseGet(() -> {
                String id = slugify(req.getCategory());
                if (categoryRepository.existsById(id)) {
                    id = id + "-" + System.currentTimeMillis();
                }
                Category c = Category.builder().id(id).name(req.getCategory()).build();
                return categoryRepository.save(c);
            });
            product.setCategory(category);
        }
    }

    private Integer computeTotalStock(ProductRequest req) {
        int s1 = req.getStock1Kg() == null ? 0 : req.getStock1Kg();
        int s5 = req.getStock5Kg() == null ? 0 : req.getStock5Kg();
        int s10 = req.getStock10Kg() == null ? 0 : req.getStock10Kg();
        int s50 = req.getStock50Kg() == null ? 0 : req.getStock50Kg();
        return s1 + s5 * 5 + s10 * 10 + s50 * 50;
    }

    private String slugify(String s) {
        if (s == null) return null;
        String slug = s.strip().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        if (slug.startsWith("-")) slug = slug.substring(1);
        if (slug.endsWith("-")) slug = slug.substring(0, slug.length() - 1);
        if (slug.isEmpty()) slug = "item-" + System.currentTimeMillis();
        return slug;
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
                .showInTodaysOffers(p.isShowInTodaysOffers())
                .displayPriority(p.getDisplayPriority())
                .offerEndDate(p.getOfferEndDate())
                .lowStockThreshold(p.getLowStockThreshold())
                .mrp(p.getMrp())
                .stock(p.getStock())
                .stock1Kg(p.getStock1Kg())
                .stock5Kg(p.getStock5Kg())
                .stock10Kg(p.getStock10Kg())
                .stock50Kg(p.getStock50Kg())
                .minOrder(p.getMinOrder())
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
