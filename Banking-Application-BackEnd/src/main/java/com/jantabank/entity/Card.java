package com.jantabank.entity;

import com.jantabank.domain.enums.CardNetwork;
import com.jantabank.domain.enums.CardStatus;
import com.jantabank.domain.enums.CardType;
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
 * A payment card issued against a customer account. Sensitive material (CVV, PIN)
 * is stored only as a salted hash; the PAN is stored in full but never exposed
 * beyond its masked form.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cards")
public class Card extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", length = 19, nullable = false, unique = true)
    private String cardNumber;

    @Column(name = "card_holder_name", length = 150, nullable = false)
    private String cardHolderName;

    @Column(name = "account_number", length = 50, nullable = false)
    private String accountNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", length = 20, nullable = false)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "network", length = 20, nullable = false)
    private CardNetwork network;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CardStatus status;

    @Column(name = "expiry_month", nullable = false)
    private int expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private int expiryYear;

    @Column(name = "cvv_hash", length = 100, nullable = false)
    private String cvvHash;

    @Column(name = "pin_hash", length = 100)
    private String pinHash;

    @Column(name = "international_enabled", nullable = false)
    private boolean internationalEnabled;

    @Column(name = "online_enabled", nullable = false)
    private boolean onlineEnabled;

    @Column(name = "contactless_enabled", nullable = false)
    private boolean contactlessEnabled;

    @Column(name = "atm_daily_limit", nullable = false)
    private double atmDailyLimit;

    @Column(name = "pos_daily_limit", nullable = false)
    private double posDailyLimit;

    @Column(name = "online_daily_limit", nullable = false)
    private double onlineDailyLimit;

    @Column(name = "blocked_reason", length = 255)
    private String blockedReason;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;
}
