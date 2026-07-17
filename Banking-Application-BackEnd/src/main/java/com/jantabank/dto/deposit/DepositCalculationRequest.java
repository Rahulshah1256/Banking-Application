package com.jantabank.dto.deposit;

import com.jantabank.domain.enums.DepositType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Pure maturity/interest calculator request; no persistence. For FIXED the
 * amount is the lump-sum principal; for RECURRING it is the monthly installment.
 */
@Getter
@Setter
public class DepositCalculationRequest {

    @NotNull(message = "Deposit type is required")
    private DepositType depositType;

    @Positive(message = "Amount must be greater than zero")
    private double amount;

    @Min(value = 3, message = "Tenure must be at least 3 months")
    @Max(value = 120, message = "Tenure cannot exceed 120 months")
    private int tenureMonths;
}
