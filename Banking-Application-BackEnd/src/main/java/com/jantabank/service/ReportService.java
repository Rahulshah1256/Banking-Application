package com.jantabank.service;

import com.jantabank.dto.report.PortfolioReportResponse;
import com.jantabank.dto.report.TransactionReportResponse;

import java.time.LocalDate;

public interface ReportService {

    TransactionReportResponse myTransactionReport(String username, LocalDate from, LocalDate to);

    PortfolioReportResponse myPortfolio(String username);
}
