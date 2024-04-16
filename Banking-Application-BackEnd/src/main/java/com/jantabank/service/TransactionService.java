package com.jantabank.service;

import com.jantabank.dto.TransactionDto;

public interface TransactionService {

    TransactionDto addTransaction(TransactionDto transactionDto, long userId);

}
