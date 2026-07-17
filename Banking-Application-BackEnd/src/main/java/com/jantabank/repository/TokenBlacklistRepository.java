package com.jantabank.repository;

import com.jantabank.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByJti(String jti);

    @Modifying
    void deleteByExpiryDateBefore(LocalDateTime cutoff);
}
