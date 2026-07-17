package com.jantabank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.jantabank.domain.enums.BeneficiaryStatus;

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

    private BeneficiaryStatus status;
}
