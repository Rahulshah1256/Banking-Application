package com.jantabank.repository;

import com.jantabank.entity.LoginOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LoginOtpRepository extends JpaRepository<LoginOtp, Long> {

    Optional<LoginOtp> findFirstByUserIdAndConsumedFalseOrderByIdDesc(long userId);

    @Modifying
    @Query("UPDATE LoginOtp o SET o.consumed = true WHERE o.user.id = :userId AND o.consumed = false")
    int consumeAllForUser(@Param("userId") long userId);

    @Modifying
    void deleteByExpiryDateBefore(LocalDateTime cutoff);
}
