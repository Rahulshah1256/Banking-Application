package com.jantabank.dto.cheque;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Positive-pay registration: pre-declare a cheque's key details so the bank can
 * validate them at clearing time.
 */
@Getter
@Setter
public class PositivePayRequest {

    @Positive(message = "Amount must be greater than zero")
    private double amount;

    @NotBlank(message = "Payee name is required")
    @Size(max = 150)
    private String payeeName;

    @NotNull(message = "Cheque date is required")
    private LocalDate chequeDate;
}
