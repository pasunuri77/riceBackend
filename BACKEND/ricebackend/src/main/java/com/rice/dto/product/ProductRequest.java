package com.rice.dto.product;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
    private BigDecimal mrp;
    private Integer stock;
    private Integer minOrder;
    private Integer maxOrder;
    private String image;
    private String status;
    private List<Integer> weightOptions;
    private List<String> badges;
}
