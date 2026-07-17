package com.jantabank.service;

import com.jantabank.dto.deposit.AutoRenewRequest;
import com.jantabank.dto.deposit.DepositCalculationRequest;
import com.jantabank.dto.deposit.DepositCalculationResponse;
import com.jantabank.dto.deposit.DepositResponse;
import com.jantabank.dto.deposit.OpenFixedDepositRequest;
import com.jantabank.dto.deposit.OpenRecurringDepositRequest;

import java.util.List;

public interface DepositService {

    DepositCalculationResponse calculate(DepositCalculationRequest request);

    DepositResponse openFixed(OpenFixedDepositRequest request, String username);

    DepositResponse openRecurring(OpenRecurringDepositRequest request, String username);

    List<DepositResponse> listMine(String username);

    DepositResponse get(Long depositId, String username);

    DepositResponse payInstallment(Long depositId, String username);

    DepositResponse close(Long depositId, String username);

    DepositResponse setAutoRenew(Long depositId, AutoRenewRequest request, String username);

    // Maturity sweep (invoked by scheduler through the proxy).
    List<Long> maturedDepositIds();

    void processMaturity(Long depositId);
}
