package com.rice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Token-based password setup links for newly invited staff/customers.
 * Expires after 24 hours. Used instead of OTP for staff invitations.
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    @Builder.Default
    private Instant expiresAt = Instant.now().plusSeconds(86400); // 24 hours

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
