package com.jantabank.entity;

import com.jantabank.domain.enums.RepaymentType;
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

import java.time.LocalDateTime;

/**
 * A single repayment ledger entry (EMI or prepayment) against a loan.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "loan_repayments")
public class LoanRepayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_id", nullable = false)
    private Long loanId;

    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_type", length = 20, nullable = false)
    private RepaymentType repaymentType;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "principal_component", nullable = false)
    private double principalComponent;

    @Column(name = "interest_component", nullable = false)
    private double interestComponent;

    @Column(name = "outstanding_after", nullable = false)
    private double outstandingAfter;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;
}
