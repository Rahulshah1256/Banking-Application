package com.jantabank.entity;

import com.jantabank.domain.enums.ChequeBookStatus;
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
 * A requested/issued cheque book tied to a customer account. Individual leaves
 * are represented by {@link Cheque} rows.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cheque_books")
public class ChequeBook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_reference_number", length = 40, nullable = false, unique = true)
    private String bookReferenceNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_number", length = 50, nullable = false)
    private String accountNumber;

    @Column(name = "number_of_leaves", nullable = false)
    private int numberOfLeaves;

    @Column(name = "start_cheque_number", nullable = false)
    private long startChequeNumber;

    @Column(name = "end_cheque_number", nullable = false)
    private long endChequeNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ChequeBookStatus status;

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}
