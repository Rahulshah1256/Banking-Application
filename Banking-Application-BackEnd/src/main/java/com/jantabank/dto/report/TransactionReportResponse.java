package com.jantabank.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReportResponse {
    private String scope;
    private LocalDate fromDate;
    private LocalDate toDate;
    private long totalCount;
    private double totalVolume;
    private double totalDebit;
    private double totalCredit;
    private List<AmountGroup> byMode;
    private List<AmountGroup> byType;
    private List<AmountGroup> byStatus;
}
