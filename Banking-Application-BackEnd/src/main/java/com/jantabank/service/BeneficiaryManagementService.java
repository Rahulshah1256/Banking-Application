package com.jantabank.service;

import com.jantabank.dto.beneficiary.BeneficiaryDetailDto;
import com.jantabank.dto.beneficiary.CreateBeneficiaryRequest;
import com.jantabank.dto.beneficiary.UpdateBeneficiaryRequest;
import com.jantabank.domain.enums.BeneficiaryStatus;

import java.util.List;

/**
 * Enriched beneficiary lifecycle management: registration with an activation
 * delay, update, delete, approval, favouriting and search. All operations are
 * scoped to the authenticated customer's own accounts.
 */
public interface BeneficiaryManagementService {

    BeneficiaryDetailDto register(CreateBeneficiaryRequest request, String username);

    BeneficiaryDetailDto update(long id, UpdateBeneficiaryRequest request, String username);

    void delete(long id, String username);

    BeneficiaryDetailDto approve(long id, String username, boolean isAdmin);

    BeneficiaryDetailDto toggleFavourite(long id, String username);

    List<BeneficiaryDetailDto> search(String username, BeneficiaryStatus status, Boolean favourite, String query);
}
