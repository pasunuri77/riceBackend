package com.rice.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponse {
    private String id;
    private String name;
    private String brand;
    private String brandId;
    private String category;
    private String type;
    private String origin;
    private String grainLength;
    private String description;
    private BigDecimal pricePerKg;
    private BigDecimal mrp;
    private Integer stock;
    private Integer minOrder;
    private Integer maxOrder;
    private Double rating;
    private Integer reviews;
    private String image;
    private List<String> images;
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
