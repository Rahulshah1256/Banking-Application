package com.jantabank.repository;

import com.jantabank.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.user.id = :userId AND t.used = false")
    int invalidateAllForUser(@Param("userId") long userId);

    @Modifying
    void deleteByExpiryDateBefore(LocalDateTime cutoff);
}
