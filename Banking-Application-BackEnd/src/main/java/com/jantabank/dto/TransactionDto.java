package com.jantabank.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

    private long id;

    private String fromaccount;

    private String toaccount;

    private double amount;

    private Date transactiondate;

    private long status;
}
