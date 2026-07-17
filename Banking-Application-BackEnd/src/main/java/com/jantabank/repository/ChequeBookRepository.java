package com.jantabank.repository;

import com.jantabank.entity.ChequeBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChequeBookRepository extends JpaRepository<ChequeBook, Long> {

    List<ChequeBook> findByUserIdOrderByIdDesc(Long userId);

    Optional<ChequeBook> findByIdAndUserId(Long id, Long userId);

    boolean existsByBookReferenceNumber(String bookReferenceNumber);
}
