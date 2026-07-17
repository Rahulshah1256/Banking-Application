package com.jantabank.dto.account;

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
public class StatementItemDto {
    private long id;
    private Date valueDate;
    /** DEBIT when money left this account, CREDIT when it arrived. */
    private String direction;
    private String counterpartyAccount;
    private double amount;
    private String status;
}
