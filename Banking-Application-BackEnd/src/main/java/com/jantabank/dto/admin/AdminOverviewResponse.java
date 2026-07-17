package com.jantabank.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOverviewResponse {

    private long totalUsers;
    private Map<String, Long> usersByStatus;

    private long totalAccounts;
    private double totalBalance;

    private long totalTransactions;
    private double totalTransactionVolume;

    private long activeLoans;
    private double totalLoanOutstanding;

    private long activeDeposits;
    private double totalDepositPrincipal;

    private long totalCards;
    private Map<String, Long> cardsByStatus;

    private long openSupportTickets;
    private long pendingKycDocuments;
}
