package com.jantabank.entity;

import com.jantabank.domain.enums.DepositStatus;
import com.jantabank.domain.enums.DepositType;
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
 * A fixed or recurring deposit held by a customer, funded from and maturing
 * back into a linked savings/current account.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "deposits")
public class Deposit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deposit_reference_number", length = 40, nullable = false, unique = true)
    private String depositReferenceNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "linked_account_number", length = 50, nullable = false)
    private String linkedAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_type", length = 20, nullable = false)
    private DepositType depositType;

    @Column(name = "principal", nullable = false)
    private double principal;

    @Column(name = "installment_amount", nullable = false)
    private double installmentAmount;

    @Column(name = "installments_paid", nullable = false)
    private int installmentsPaid;

    @Column(name = "annual_interest_rate", nullable = false)
    private double annualInterestRate;

    @Column(name = "tenure_months", nullable = false)
    private int tenureMonths;

    @Column(name = "maturity_amount", nullable = false)
    private double maturityAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private DepositStatus status;

    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}
