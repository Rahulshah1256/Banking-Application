package com.jantabank.dto.upi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request to pay a payee VPA. The payer is identified either by one of their own
 * VPAs ({@code payerVpa}) or directly by {@code fromAccountNumber}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpiPayRequest {

    private String payerVpa;

    private String fromAccountNumber;

    @NotBlank(message = "Payee VPA is required")
    private String payeeVpa;

    @Positive(message = "Amount must be greater than zero")
    private double amount;

    @Size(max = 255, message = "Note must not exceed 255 characters")
    private String note;
}
