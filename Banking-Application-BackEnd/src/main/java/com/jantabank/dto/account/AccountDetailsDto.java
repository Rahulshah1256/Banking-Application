package com.jantabank.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsDto {
    private long id;
    private String accountNumber;
    private String accountHolderName;
    private String accountType;
    private String branchId;
    private String ifscCode;
    private String status;
    private Date openDate;
    private String address;
    private String contactNumber;
    private String emailAddress;
    private String nominee;
    private double balance;
    private double availableBalance;
}
