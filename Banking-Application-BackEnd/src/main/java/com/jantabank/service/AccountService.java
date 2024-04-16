package com.jantabank.service;
import com.jantabank.dto.AccountDto;
import com.jantabank.entity.Account;

import java.util.List;

public interface AccountService {
    AccountDto addAccount(AccountDto AccountDto);

    AccountDto getAccount(long id);

    Account getAccountByAccountNumber(String accountNumber);

    List<AccountDto> getAllAccounts();

    List<AccountDto> getAccountByUserid(long id);

    List<Account> getAccountByUser(long id);

    void deleteAccount(long id);

    Account updateAccount(Account account);

}
