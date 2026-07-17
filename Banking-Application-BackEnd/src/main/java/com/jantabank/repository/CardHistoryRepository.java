package com.jantabank.repository;

import com.jantabank.entity.CardHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardHistoryRepository extends JpaRepository<CardHistory, Long> {

    List<CardHistory> findByCardIdOrderByIdDesc(Long cardId);
}
