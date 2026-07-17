package com.jantabank.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryDto {
    private long id;
    private String maskedAccountNumber;
    private String accountType;
    private String status;
    private double currentBalance;
    private double availableBalance;
    private long accountAgeDays;
    private double annualInterestRate;
    private double projectedAnnualInterest;
    private String nomineeName;
    private String kycStatus;
    private String chequeBookStatus;
}
