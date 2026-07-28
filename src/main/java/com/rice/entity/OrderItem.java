package com.rice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // snapshots so historical order items stay correct even if the product changes later
    @Column(name = "product_name_snapshot", nullable = false)
    private String productNameSnapshot;

    @Column(name = "image_snapshot")
    private String imageSnapshot;

    @Column(name = "weight_kg", nullable = false)
    private Integer weightKg;

    @Column(nullable = false)
    private Integer qty;

    @Column(name = "price_per_kg_snapshot", nullable = false)
    private BigDecimal pricePerKgSnapshot;
}
