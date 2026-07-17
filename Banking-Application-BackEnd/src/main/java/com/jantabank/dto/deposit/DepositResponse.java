package com.jantabank.dto.deposit;

import com.jantabank.domain.enums.DepositStatus;
import com.jantabank.domain.enums.DepositType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class DepositResponse {
    private Long id;
    private String depositReferenceNumber;
    private DepositType depositType;
    private String linkedAccountNumber;
    private double principal;
    private double installmentAmount;
    private int installmentsPaid;
    private double annualInterestRate;
    private int tenureMonths;
    private double maturityAmount;
    private DepositStatus status;
    private boolean autoRenew;
    private LocalDateTime openedAt;
    private LocalDate maturityDate;
    private LocalDateTime closedAt;
}
