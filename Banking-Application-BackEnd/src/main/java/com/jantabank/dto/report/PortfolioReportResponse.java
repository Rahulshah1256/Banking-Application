package com.jantabank.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioReportResponse {

    private long accountsCount;
    private double totalBalance;
    private List<AccountHolding> accounts;

    private long loansCount;
    private double totalLoanOutstanding;

    private long depositsCount;
    private double totalDepositPrincipal;
    private double totalDepositMaturityValue;

    private long cardsCount;
    private long activeCards;

    private double netWorth;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountHolding {
        private String maskedAccountNumber;
        private String accountType;
        private double balance;
        private String status;
    }
}
