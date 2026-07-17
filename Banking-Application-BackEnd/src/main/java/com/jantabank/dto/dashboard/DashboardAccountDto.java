package com.jantabank.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAccountDto {
    private long id;
    private String maskedAccountNumber;
    private String accountType;
    private String ifscCode;
    private String status;
    private double balance;
    private double availableBalance;
}
