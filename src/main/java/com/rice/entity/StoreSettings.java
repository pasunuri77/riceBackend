package com.rice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "store_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreSettings {

    @Id
    private Long id;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "gst_number")
    private String gstNumber;

    private String phone;

    private String email;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "delivery_charge", nullable = false)
    private BigDecimal deliveryCharge;

    @Column(name = "free_delivery_threshold", nullable = false)
    private BigDecimal freeDeliveryThreshold;

    @Column(name = "tax_percentage", nullable = false)
    private BigDecimal taxPercentage;

    @Column(name = "business_hours", columnDefinition = "text")
    private String businessHours;
}
