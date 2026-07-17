package com.jantabank.dto.admin;

import com.jantabank.domain.enums.TransactionStatus;
import com.jantabank.domain.enums.TransactionType;
import com.jantabank.domain.enums.TransferMode;
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
public class AdminTransactionView {
    private long id;
    private String referenceNumber;
    private String maskedFromAccount;
    private String maskedToAccount;
    private double amount;
    private Date transactionDate;
    private TransferMode transferMode;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String channel;
    private Long initiatedByUserId;
}
