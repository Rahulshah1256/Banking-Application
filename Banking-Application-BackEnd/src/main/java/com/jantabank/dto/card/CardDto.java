package com.jantabank.dto.card;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Read model for a card. The PAN is always masked; CVV/PIN are never exposed.
 */
@Getter
@Setter
@Builder
public class CardDto {

    private Long id;
    private String maskedCardNumber;
    private String cardHolderName;
    private String maskedAccountNumber;
    private String cardType;
    private String network;
    private String status;
    private String expiry;
    private boolean pinSet;
    private boolean internationalEnabled;
    private boolean onlineEnabled;
    private boolean contactlessEnabled;
    private double atmDailyLimit;
    private double posDailyLimit;
    private double onlineDailyLimit;
    private String blockedReason;
    private LocalDateTime issuedAt;
}
