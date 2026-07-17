package com.jantabank.dto.txn;

import com.jantabank.domain.enums.ScheduleFrequency;
import com.jantabank.domain.enums.TransferMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request to create a scheduled (optionally recurring) transfer instruction.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleTransferRequest {

    @NotBlank(message = "Source account number is required")
    private String fromAccountNumber;

    @NotBlank(message = "Destination account number is required")
    private String toAccountNumber;

    @Positive(message = "Amount must be greater than zero")
    private double amount;

    @NotNull(message = "Transfer mode is required")
    private TransferMode transferMode;

    @NotNull(message = "Frequency is required")
    private ScheduleFrequency frequency;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}
