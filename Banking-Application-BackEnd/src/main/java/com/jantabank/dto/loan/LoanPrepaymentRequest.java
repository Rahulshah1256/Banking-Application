package com.jantabank.dto.loan;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Prepayment (part or full) against an active loan's outstanding principal.
 */
@Getter
@Setter
public class LoanPrepaymentRequest {

    @Positive(message = "Prepayment amount must be greater than zero")
    private double amount;
}
