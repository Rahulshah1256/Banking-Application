package com.jantabank.service;

import com.jantabank.domain.enums.ChequeStatus;
import com.jantabank.dto.cheque.ChequeBookRequest;
import com.jantabank.dto.cheque.ChequeBookResponse;
import com.jantabank.dto.cheque.ChequeResponse;
import com.jantabank.dto.cheque.PositivePayRequest;
import com.jantabank.dto.cheque.StopChequeRequest;

import java.util.List;

public interface ChequeService {

    ChequeBookResponse requestBook(ChequeBookRequest request, String username);

    List<ChequeBookResponse> listBooks(String username);

    ChequeBookResponse getBook(Long bookId, String username);

    ChequeBookResponse issueBook(Long bookId);

    ChequeBookResponse deliverBook(Long bookId);

    List<ChequeResponse> history(String username, ChequeStatus status, String accountNumber);

    ChequeResponse getCheque(Long chequeId, String username);

    ChequeResponse stopCheque(Long chequeId, StopChequeRequest request, String username);

    ChequeResponse registerPositivePay(Long chequeId, PositivePayRequest request, String username);
}
