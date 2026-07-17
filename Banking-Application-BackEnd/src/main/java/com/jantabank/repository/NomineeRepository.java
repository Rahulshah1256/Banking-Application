package com.jantabank.repository;

import com.jantabank.entity.Nominee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NomineeRepository extends JpaRepository<Nominee, Long> {

    List<Nominee> findByUserIdOrderByIdAsc(Long userId);

    Optional<Nominee> findByIdAndUserId(Long id, Long userId);
}
