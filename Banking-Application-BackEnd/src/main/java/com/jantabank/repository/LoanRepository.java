package com.jantabank.repository;

import com.jantabank.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserIdOrderByIdDesc(Long userId);

    Optional<Loan> findByIdAndUserId(Long id, Long userId);

    boolean existsByLoanReferenceNumber(String loanReferenceNumber);

    long countByStatus(com.jantabank.domain.enums.LoanStatus status);

    long countByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(l.outstandingPrincipal), 0) FROM Loan l WHERE l.status = :status")
    double sumOutstandingByStatus(@org.springframework.data.repository.query.Param("status") com.jantabank.domain.enums.LoanStatus status);
}
