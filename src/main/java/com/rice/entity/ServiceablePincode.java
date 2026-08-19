package com.rice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "serviceable_pincodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceablePincode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String pincode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private DeliveryZone zone;

    @org.hibernate.annotations.ColumnDefault("true")
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @org.hibernate.annotations.ColumnDefault("false")
    @Column(name = "is_named_zone", nullable = false)
    @Builder.Default
    private boolean isNamedZone = false;
    
    // For backward compatibility constructors
    public ServiceablePincode(Long id, String pincode) {
        this.id = id;
        this.pincode = pincode;
        this.active = true;
        this.isNamedZone = false;
    }
}
