package com.rice.entity;

import com.rice.entity.enums.DeliveryStatus;
import com.rice.entity.enums.PaymentMethod;
import com.rice.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    // snapshot of the delivery address text at order time - addresses can be edited/deleted later
    @Column(name = "address_snapshot", columnDefinition = "text")
    private String addressSnapshot;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "discount_amount")
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_provider")
    private String paymentProvider;

    @Column(name = "payment_display")
    private String paymentDisplay;

    @Column(name = "payment_reference")
    private String paymentReference;

    @org.hibernate.annotations.ColumnDefault("'online'")
    @Column(name = "order_type", nullable = false, length = 20)
    @Builder.Default
    private String orderType = "online";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    @Builder.Default
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "estimated_delivery_date")
    private Instant estimatedDeliveryDate;

    @Column(name = "delay_flag", nullable = false)
    @Builder.Default
    private boolean delayFlag = false;

    @Column(name = "delivery_remarks", columnDefinition = "text")
    private String deliveryRemarks;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "delivery_charge", nullable = false)
    @Builder.Default
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Column(name = "offer_discount", nullable = false)
    @Builder.Default
    private BigDecimal offerDiscount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
