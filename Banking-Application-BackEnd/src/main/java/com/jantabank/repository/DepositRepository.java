package com.jantabank.repository;

import com.jantabank.domain.enums.DepositStatus;
import com.jantabank.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DepositRepository extends JpaRepository<Deposit, Long> {

    List<Deposit> findByUserIdOrderByIdDesc(Long userId);

    Optional<Deposit> findByIdAndUserId(Long id, Long userId);

    boolean existsByDepositReferenceNumber(String depositReferenceNumber);

    long countByStatus(DepositStatus status);

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(d.principal), 0) FROM Deposit d WHERE d.status = :status")
    double sumPrincipalByStatus(@Param("status") DepositStatus status);

    @Query("SELECT d.id FROM Deposit d WHERE d.status = :status AND d.maturityDate <= :onOrBefore")
    List<Long> findMaturedIds(@Param("status") DepositStatus status, @Param("onOrBefore") LocalDate onOrBefore);
}
