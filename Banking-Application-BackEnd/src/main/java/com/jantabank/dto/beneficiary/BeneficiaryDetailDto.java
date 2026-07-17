package com.jantabank.dto.beneficiary;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Read model for a beneficiary, including lifecycle/activation-delay details.
 */
@Getter
@Setter
@Builder
public class BeneficiaryDetailDto {

    private long id;
    private String beneficiaryAccountNumber;
    private String maskedAccountNumber;
    private String beneficiaryAccountName;
    private String beneficiaryAccountIfsc;
    private double amountLimit;
    private String nickname;
    private String status;
    private boolean favourite;
    private boolean payable;
    private LocalDateTime activateAfter;
}
