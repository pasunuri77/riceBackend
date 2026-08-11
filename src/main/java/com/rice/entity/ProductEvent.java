package com.rice.entity;

import com.rice.entity.enums.ProductEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "product_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ProductEventType type;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
