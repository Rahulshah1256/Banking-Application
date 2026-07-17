package com.jantabank.service;

import com.jantabank.dto.loan.AmortizationEntry;
import com.jantabank.dto.loan.LoanApplicationRequest;
import com.jantabank.dto.loan.LoanCalculationRequest;
import com.jantabank.dto.loan.LoanCalculationResponse;
import com.jantabank.dto.loan.LoanPrepaymentRequest;
import com.jantabank.dto.loan.LoanRepaymentResponse;
import com.jantabank.dto.loan.LoanResponse;

import java.util.List;

public interface LoanService {

    LoanCalculationResponse calculate(LoanCalculationRequest request);

    LoanResponse apply(LoanApplicationRequest request, String username);

    List<LoanResponse> listMine(String username);

    LoanResponse get(Long loanId, String username);

    List<AmortizationEntry> schedule(Long loanId, String username);

    List<LoanRepaymentResponse> statement(Long loanId, String username);

    LoanResponse payEmi(Long loanId, String username);

    LoanResponse prepay(Long loanId, LoanPrepaymentRequest request, String username);
}
