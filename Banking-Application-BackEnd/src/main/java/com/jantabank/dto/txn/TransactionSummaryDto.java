package com.jantabank.dto.txn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Compact history-row view of a transaction, relative to the requesting customer.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryDto {
    private long id;
    private String referenceNumber;
    private String direction;
    private String counterpartyAccount;
    private double amount;
    private String transferMode;
    private String status;
    private String description;
    private Date transactionDate;
}
