package com.jantabank.repository;

import com.jantabank.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

    List<KycDocument> findByUserIdOrderByIdDesc(Long userId);

    Optional<KycDocument> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByStatus(com.jantabank.domain.enums.KycStatus status);
}
