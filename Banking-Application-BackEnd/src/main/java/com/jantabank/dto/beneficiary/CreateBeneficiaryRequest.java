package com.jantabank.dto.beneficiary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request to register a new beneficiary against one of the caller's accounts.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBeneficiaryRequest {

    @NotBlank(message = "Beneficiary account number is required")
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    private String beneficiaryAccountNumber;

    @NotBlank(message = "Beneficiary account name is required")
    @Size(max = 150, message = "Account name must not exceed 150 characters")
    private String beneficiaryAccountName;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Za-z]{4}0[A-Za-z0-9]{6}$", message = "Invalid IFSC code")
    private String beneficiaryAccountIfsc;

    @PositiveOrZero(message = "Amount limit cannot be negative")
    private double amountLimit;

    @Size(max = 100, message = "Nickname must not exceed 100 characters")
    private String nickname;

    /** Optional: link the beneficiary to a specific owned account; defaults to the first. */
    private String linkAccountNumber;
}
