package com.jantabank.dto.loan;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * A single row of the projected amortisation schedule.
 */
@Getter
@Builder
public class AmortizationEntry {
    private int installmentNumber;
    private LocalDate dueDate;
    private double emiAmount;
    private double principalComponent;
    private double interestComponent;
    private double balanceAfter;
}
