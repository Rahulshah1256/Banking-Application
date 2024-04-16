package com.jantabank.service;

import com.jantabank.dto.BeneficiaryDto;

import java.util.List;

public interface BeneficiaryService {

    BeneficiaryDto addBeneficiary(BeneficiaryDto beneficiaryDto,long userId);
    List<BeneficiaryDto> getAllBeneficiaries(long userId);
}
