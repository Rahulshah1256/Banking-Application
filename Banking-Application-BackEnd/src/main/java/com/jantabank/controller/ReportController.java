package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.dto.report.PortfolioReportResponse;
import com.jantabank.dto.report.TransactionReportResponse;
import com.jantabank.service.ReportService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@CrossOrigin("*")
@RestController
@RequestMapping("api/reports")
@AllArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<TransactionReportResponse>> myTransactionReport(
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                reportService.myTransactionReport(principal.getName(), from, to), "Transaction report generated"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/portfolio")
    public ResponseEntity<ApiResponse<PortfolioReportResponse>> myPortfolio(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                reportService.myPortfolio(principal.getName()), "Portfolio report generated"));
    }
}
