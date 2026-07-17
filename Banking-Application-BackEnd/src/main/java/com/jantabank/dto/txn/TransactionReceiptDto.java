package com.jantabank.dto.txn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Full detail / receipt view of a single transaction. Account numbers are
 * masked; {@code direction} is relative to the requesting customer.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReceiptDto {
    private long id;
    private String referenceNumber;
    private String fromAccount;
    private String toAccount;
    private String direction;
    private String counterpartyAccount;
    private double amount;
    private String transferMode;
    private String transactionType;
    private String status;
    private String description;
    private String channel;
    private Date transactionDate;
}
