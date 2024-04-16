package com.jantabank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryDto {

    private long id;

    private String beneficiaryaccountnumber;

    private String beneficiaryaccountname;

    private String beneficiaryaccountifsc;

    private double amountlimit;

    private long status;
}
