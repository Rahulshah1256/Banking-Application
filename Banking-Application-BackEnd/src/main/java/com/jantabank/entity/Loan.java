package com.jantabank.entity;

import com.jantabank.domain.enums.LoanStatus;
import com.jantabank.domain.enums.LoanType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A customer loan account with an amortising EMI schedule.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "loans")
public class Loan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_reference_number", length = 40, nullable = false, unique = true)
    private String loanReferenceNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "disbursement_account_number", length = 50, nullable = false)
    private String disbursementAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", length = 20, nullable = false)
    private LoanType loanType;

    @Column(name = "principal", nullable = false)
    private double principal;

    @Column(name = "annual_interest_rate", nullable = false)
    private double annualInterestRate;

    @Column(name = "tenure_months", nullable = false)
    private int tenureMonths;

    @Column(name = "emi_amount", nullable = false)
    private double emiAmount;

    @Column(name = "outstanding_principal", nullable = false)
    private double outstandingPrincipal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private LoanStatus status;

    @Column(name = "emis_paid", nullable = false)
    private int emisPaid;

    @Column(name = "next_emi_date")
    private LocalDate nextEmiDate;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "disbursed_at")
    private LocalDateTime disbursedAt;
}
