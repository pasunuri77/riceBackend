package com.rice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "product_delivery_coverage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "pincode"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDeliveryCoverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(nullable = false)
    private String pincode;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
