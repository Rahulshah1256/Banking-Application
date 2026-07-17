package com.jantabank.dto.admin;

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
public class AdminAccountView {
    private String maskedAccountNumber;
    private String accountType;
    private double balance;
    private String status;
}
