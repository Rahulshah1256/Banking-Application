package com.jantabank.dto.loan;

import com.jantabank.domain.enums.LoanStatus;
import com.jantabank.domain.enums.LoanType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class LoanResponse {
    private Long id;
    private String loanReferenceNumber;
    private LoanType loanType;
    private String disbursementAccountNumber;
    private double principal;
    private double annualInterestRate;
    private int tenureMonths;
    private double emiAmount;
    private double outstandingPrincipal;
    private LoanStatus status;
    private int emisPaid;
    private LocalDate nextEmiDate;
    private LocalDateTime appliedAt;
    private LocalDateTime disbursedAt;
}
