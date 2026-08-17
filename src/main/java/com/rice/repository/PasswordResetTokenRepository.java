package com.rice.repository;

import com.rice.entity.PasswordResetToken;
import com.rice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUserAndExpiresAtAfter(User user, Instant now);
    void deleteByExpiresAtBefore(Instant now);
    void deleteByUser(User user);
}
