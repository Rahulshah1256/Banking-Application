package com.jantabank.dto.txn;

import com.jantabank.domain.enums.TransferMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotBlank(message = "Source account number is required")
    private String fromAccountNumber;

    @NotBlank(message = "Destination account number is required")
    private String toAccountNumber;

    @Positive(message = "Amount must be greater than zero")
    private double amount;

    @NotNull(message = "Transfer mode is required")
    private TransferMode transferMode;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}
