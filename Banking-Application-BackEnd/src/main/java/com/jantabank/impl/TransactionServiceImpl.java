package com.jantabank.impl;

import com.jantabank.config.Enums;
import com.jantabank.dto.TransactionDto;
import com.jantabank.entity.Account;
import com.jantabank.entity.Transaction;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.repository.TransactionRepository;
import com.jantabank.service.AccountService;
import com.jantabank.service.TransactionService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private ModelMapper modelMapper;
    private TransactionRepository transactionRepository ;
    private AccountService accountService;
    private final TransactionTemplate transactionTemplate;
    @Override
    public TransactionDto addTransaction(TransactionDto transactionDto, long userId) {

        List<Account> myAccounts = accountService.getAccountByUser(userId);

        boolean isValidFromAccount = myAccounts.stream()
                .anyMatch(account -> account.getAccountNumber().equals(transactionDto.getFromaccount()));
        if (isValidFromAccount) {

            Account fromAccount = accountService.getAccountByAccountNumber(transactionDto.getFromaccount());
            Account toAccount = accountService.getAccountByAccountNumber(transactionDto.getToaccount());
            double transferAmount = transactionDto.getAmount();
            if (fromAccount != null && toAccount != null && transferAmount > fromAccount.getBalance())
            {
                throw new ResourceNotFoundException("insufficient account balance");
            }
            assert fromAccount != null;
            fromAccount.setBalance(fromAccount.getBalance() - transferAmount);

            assert toAccount != null;
            toAccount.setBalance(toAccount.getBalance() + transferAmount);

            Transaction transaction = modelMapper.map(transactionDto, Transaction.class);

            try
            {
                // Perform the transaction in a database transaction
                transactionTemplate.execute(status -> {

                    transaction.setStatus(Enums.TransactionStatus.COMPLETED);
                    Transaction savedTransaction = transactionRepository.save(transaction);

                    accountService.updateAccount(fromAccount);
                    accountService.updateAccount(toAccount);

                    return null; // Return value not needed within the execute block
                });
                TransactionDto savedTransactionDto = modelMapper.map(transaction, TransactionDto.class);
                return savedTransactionDto;
            } catch (Exception e) {
                throw new ResourceNotFoundException("Transaction failed");
            }
        }
        else
        {
            throw new ResourceNotFoundException("Not a valid account");
        }
    }
}
