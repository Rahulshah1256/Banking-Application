package com.jantabank.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private String customerName;
    private long customerId;
    private LocalDateTime lastLoginTime;
    private String currency;
    private double totalBalance;
    private int pendingBeneficiaryRequests;
    private List<DashboardAccountDto> accounts;
    private List<MiniStatementItemDto> recentTransactions;
    private List<RecentBeneficiaryDto> recentBeneficiaries;
    private ModuleSummaryDto cards;
    private ModuleSummaryDto loans;
    private ModuleSummaryDto deposits;
}
