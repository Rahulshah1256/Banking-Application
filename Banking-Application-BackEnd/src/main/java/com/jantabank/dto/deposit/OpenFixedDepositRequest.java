package com.jantabank.dto.deposit;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Open a Fixed Deposit: a lump sum debited from the linked account.
 */
@Getter
@Setter
public class OpenFixedDepositRequest {

    @Positive(message = "Principal must be greater than zero")
    private double principal;

    @Min(value = 3, message = "Tenure must be at least 3 months")
    @Max(value = 120, message = "Tenure cannot exceed 120 months")
    private int tenureMonths;

    @NotBlank(message = "Linked account number is required")
    private String linkedAccountNumber;

    private boolean autoRenew;
}
