package com.jantabank.dto.deposit;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Open a Recurring Deposit: a fixed monthly installment. The first installment
 * is debited on opening.
 */
@Getter
@Setter
public class OpenRecurringDepositRequest {

    @Positive(message = "Monthly installment must be greater than zero")
    private double monthlyInstallment;

    @Min(value = 3, message = "Tenure must be at least 3 months")
    @Max(value = 120, message = "Tenure cannot exceed 120 months")
    private int tenureMonths;

    @NotBlank(message = "Linked account number is required")
    private String linkedAccountNumber;

    private boolean autoRenew;
}
