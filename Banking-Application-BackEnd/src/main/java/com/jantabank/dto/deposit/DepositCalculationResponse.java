package com.jantabank.dto.deposit;

import com.jantabank.domain.enums.DepositType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DepositCalculationResponse {
    private DepositType depositType;
    private double amount;
    private int tenureMonths;
    private double annualInterestRate;
    private double totalDeposited;
    private double maturityAmount;
    private double interestEarned;
}
