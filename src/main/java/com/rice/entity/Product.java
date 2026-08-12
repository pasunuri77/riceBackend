package com.rice.entity;

import com.rice.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    private String id; // e.g. "p1" - kept as-is to match existing frontend/mock data ids

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String type;
    private String origin;

    @Column(name = "grain_length")
    private String grainLength;

    @Column(name = "price_per_kg", nullable = false)
    private BigDecimal pricePerKg;

    @Column(name = "show_in_todays_offers", nullable = false)
    @Builder.Default
    private boolean showInTodaysOffers = false;

    @Column(name = "display_priority", nullable = false)
    @Builder.Default
    private Integer displayPriority = 0;

    @Column(name = "offer_end_date")
    private Instant offerEndDate;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    private BigDecimal mrp;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "stock_1kg")
    private Integer stock1Kg;

    @Column(name = "stock_5kg")
    private Integer stock5Kg;

    @Column(name = "stock_10kg")
    private Integer stock10Kg;

    @Column(name = "stock_50kg")
    private Integer stock50Kg;

    @Column(name = "min_order")
    private Integer minOrder;

    @Column(name = "max_order")
    private Integer maxOrder;

    @Builder.Default
    private Double rating = 0.0;

    @Column(columnDefinition = "text")
    private String description;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", length = 2000)
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Column(length = 2000)
    private String image;

    private String supplier;
    private String batchNumber;
    private String lotNumber;
    private BigDecimal costPrice;
    private String nutritionFacts;
    private String cookingInstructions;
    private String aroma;
    private String texture;
    private String riceAge;
    private Double brokenPercentage;
    private String shelfLife;
    private String storageInstructions;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @ElementCollection
    @CollectionTable(name = "product_weight_options", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "weight_kg")
    @Builder.Default
    private List<Integer> weightOptions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "product_badges", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "badge")
    @Builder.Default
    private List<String> badges = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
