package com.jantabank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A UPI Virtual Payment Address (e.g. {@code alice@jantabank}) that resolves to a
 * single customer account for collect/pay operations.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "upi_handles")
public class UpiHandle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vpa", length = 100, nullable = false, unique = true)
    private String vpa;

    @Column(name = "account_number", length = 50, nullable = false)
    private String accountNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "active", nullable = false)
    private boolean active;
}
