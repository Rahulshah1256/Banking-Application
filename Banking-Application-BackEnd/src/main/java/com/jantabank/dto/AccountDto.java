package com.jantabank.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    private long id;

    private String accountNumber;

    private String accountHolderName;

    private String accountType;

    private String branchId;

    private String ifscCode;

    public double balance;

    private String openDate;

    private String address;

    private String contactNumber;

    private String emailAddress;

    private String nominee;

}

