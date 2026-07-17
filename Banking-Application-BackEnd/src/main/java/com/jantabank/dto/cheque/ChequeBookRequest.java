package com.jantabank.dto.cheque;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request a new cheque book against an owned account.
 */
@Getter
@Setter
public class ChequeBookRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Number of leaves is required")
    private Integer numberOfLeaves;

    @Size(max = 500, message = "Delivery address is too long")
    private String deliveryAddress;
}
