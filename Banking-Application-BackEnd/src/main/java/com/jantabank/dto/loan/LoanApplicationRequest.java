package com.jantabank.dto.loan;

import com.jantabank.domain.enums.LoanType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Loan application request. On approval the principal is disbursed to the
 * given account and an amortising EMI schedule begins.
 */
@Getter
@Setter
public class LoanApplicationRequest {

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @Positive(message = "Principal must be greater than zero")
    private double principal;

    @Min(value = 3, message = "Tenure must be at least 3 months")
    @Max(value = 360, message = "Tenure cannot exceed 360 months")
    private int tenureMonths;

    @NotBlank(message = "Disbursement account number is required")
    private String disbursementAccountNumber;
}
