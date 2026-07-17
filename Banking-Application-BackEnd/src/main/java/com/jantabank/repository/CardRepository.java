package com.jantabank.repository;

import com.jantabank.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByUserIdOrderByIdDesc(Long userId);

    Optional<Card> findByIdAndUserId(Long id, Long userId);

    boolean existsByCardNumber(String cardNumber);

    long countByStatus(com.jantabank.domain.enums.CardStatus status);

    long countByUserId(Long userId);
}
