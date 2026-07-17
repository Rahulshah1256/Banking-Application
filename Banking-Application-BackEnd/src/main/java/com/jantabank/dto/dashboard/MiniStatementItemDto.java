package com.jantabank.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniStatementItemDto {
    private long id;
    /** DEBIT when money left one of the user's accounts, CREDIT when it arrived. */
    private String direction;
    private String counterpartyAccount;
    private double amount;
    private Date transactionDate;
    private String status;
}
