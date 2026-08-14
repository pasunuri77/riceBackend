package com.rice.entity;

import com.rice.entity.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String mobile;

    @Column(name = "alt_mobile")
    private String altMobile;

    private String flat;
    private String building;
    private String street;
    private String area;
    private String landmark;
    private String village;
    private String city;
    private String district;
    private String state;

    private String country;

    private String pincode;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AddressType type = AddressType.HOME;

    @Column(columnDefinition = "text")
    private String instructions;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;
}
