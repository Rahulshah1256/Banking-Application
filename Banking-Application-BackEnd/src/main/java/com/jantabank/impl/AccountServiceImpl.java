package com.jantabank.impl;
import com.jantabank.dto.AccountDto;
import com.jantabank.entity.Account;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.repository.AccountRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.jantabank.service.AccountService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {

    private ModelMapper modelMapper;
    private AccountRepository accountRepository;

    @Override
    public AccountDto addAccount(AccountDto accountDto) {

        //convert account entity to jpa entity
        Account account = modelMapper.map(accountDto,Account.class);

        //account jpa entity
        Account savedAccount = accountRepository.save(account);

        //save account entity to database
        AccountDto savedAccountDto = modelMapper.map(savedAccount, AccountDto.class);

        return savedAccountDto;
    }

    @Override
    public AccountDto getAccount(long id) {
        Account account =  accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id : " + id));
        return modelMapper.map(account, AccountDto.class);
    }

    @Override
    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public List<AccountDto> getAccountByUserid(long id) {
        List<Account> accounts =  accountRepository.findByUsers_Id(id);

        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("No accounts found");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy"); // Define your desired date format

        return accounts.stream()
                .map(account -> {
                    AccountDto accountDto = modelMapper.map(account, AccountDto.class);
                    String formattedDate = dateFormat.format(account.getOpenDate());
                    accountDto.setOpenDate(formattedDate);
                    return accountDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> getAccountByUser(long id) {
        return accountRepository.findByUsers_Id(id);
    }

    @Override
    public List<AccountDto> getAllAccounts() {
        List<Account> accounts =  accountRepository.findAll();

        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("No accounts found");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy"); // Define your desired date format

        return accounts.stream()
                .map(account -> {
                    AccountDto accountDto = modelMapper.map(account, AccountDto.class);
                    String formattedDate = dateFormat.format(account.getOpenDate());
                    accountDto.setOpenDate(formattedDate);
                    return accountDto;
                })
                .collect(Collectors.toList());

    }

    @Override
    public void deleteAccount(long id) {
        Account account =  accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id : " + id));
        accountRepository.deleteById(account.getId());
    }

    @Override
    public Account updateAccount(Account account) {
        Account savedAccount =  accountRepository.findById(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id : " + account.getId()));
       return accountRepository.save(account);
    }

}

