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
}
