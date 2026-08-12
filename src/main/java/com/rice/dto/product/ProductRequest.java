package com.rice.dto.product;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ProductRequest {
    private String name;
    private String brand;    // brand name, matches the <select> value in the admin form
    private String category; // category name, matches the <select> value in the admin form
    private String type;
    private String origin;
    private String grainLength;
    private String description;
    private BigDecimal pricePerKg;
    private Boolean showInTodaysOffers;
    private Integer displayPriority;
    private Instant offerEndDate;
    private Integer lowStockThreshold;
    private BigDecimal mrp;
    private Integer stock;
    private Integer stock1Kg;
    private Integer stock5Kg;
    private Integer stock10Kg;
    private Integer stock50Kg;
    private Integer minOrder;
    private Integer maxOrder;
    private List<String> images;
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
    private String status;
    private List<Integer> weightOptions;
    private List<String> badges;
}
