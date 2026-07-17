package com.jantabank.repository;

import com.jantabank.entity.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    Page<LoginHistory> findByUserIdOrderByLoginTimeDesc(long userId, Pageable pageable);

    Optional<LoginHistory> findFirstByUserIdAndSuccessfulTrueOrderByLoginTimeDesc(long userId);
}
