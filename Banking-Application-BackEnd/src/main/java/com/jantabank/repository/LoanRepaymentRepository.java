package com.jantabank.repository;

import com.jantabank.entity.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {

    List<LoanRepayment> findByLoanIdOrderByIdAsc(Long loanId);
}
