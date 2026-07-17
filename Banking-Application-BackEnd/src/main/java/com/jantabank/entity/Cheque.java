package com.jantabank.entity;

import com.jantabank.domain.enums.ChequeStatus;
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
 * A single cheque leaf. Supports stop-payment and positive-pay registration.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cheques")
public class Cheque extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cheque_book_id", nullable = false)
    private Long chequeBookId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_number", length = 50, nullable = false)
    private String accountNumber;

    @Column(name = "cheque_number", nullable = false, unique = true)
    private long chequeNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ChequeStatus status;

    @Column(name = "stop_reason", length = 255)
    private String stopReason;

    @Column(name = "stopped_at")
    private LocalDateTime stoppedAt;

    @Column(name = "positive_pay_registered", nullable = false)
    private boolean positivePayRegistered;

    @Column(name = "positive_pay_amount")
    private Double positivePayAmount;

    @Column(name = "positive_pay_payee", length = 150)
    private String positivePayPayee;

    @Column(name = "positive_pay_date")
    private LocalDate positivePayDate;
}
