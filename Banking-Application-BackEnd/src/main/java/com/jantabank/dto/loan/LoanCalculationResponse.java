package com.jantabank.dto.loan;

import com.jantabank.domain.enums.LoanType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * EMI-calculator result.
 */
@Getter
@AllArgsConstructor
public class LoanCalculationResponse {
    private LoanType loanType;
    private double principal;
    private double annualInterestRate;
    private int tenureMonths;
    private double emiAmount;
    private double totalPayable;
    private double totalInterest;
}
