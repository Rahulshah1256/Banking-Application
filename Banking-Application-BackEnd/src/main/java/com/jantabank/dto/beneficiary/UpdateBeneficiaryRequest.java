package com.jantabank.dto.beneficiary;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request to update mutable attributes of an existing beneficiary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBeneficiaryRequest {

    @Size(max = 150, message = "Account name must not exceed 150 characters")
    private String beneficiaryAccountName;

    @PositiveOrZero(message = "Amount limit cannot be negative")
    private double amountLimit;

    @Size(max = 100, message = "Nickname must not exceed 100 characters")
    private String nickname;
}
