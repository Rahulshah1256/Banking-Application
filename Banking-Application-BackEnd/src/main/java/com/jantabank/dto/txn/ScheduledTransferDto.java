package com.jantabank.dto.txn;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read model for a scheduled/recurring transfer instruction.
 */
@Getter
@Setter
@Builder
public class ScheduledTransferDto {

    private Long id;
    private String referenceNumber;
    private String fromAccount;
    private String toAccount;
    private double amount;
    private String transferMode;
    private String frequency;
    private String status;
    private String description;
    private LocalDate nextRunDate;
    private LocalDateTime lastRunAt;
    private int executionsCount;
    private String lastError;
}
