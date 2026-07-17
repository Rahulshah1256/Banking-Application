package com.jantabank.dto.loan;

import com.jantabank.domain.enums.RepaymentType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LoanRepaymentResponse {
    private Long id;
    private RepaymentType repaymentType;
    private double amount;
    private double principalComponent;
    private double interestComponent;
    private double outstandingAfter;
    private LocalDateTime paidAt;
}
