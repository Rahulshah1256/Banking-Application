package com.jantabank.impl;

import com.jantabank.dto.LoginHistoryDto;
import com.jantabank.dto.dashboard.DashboardAccountDto;
import com.jantabank.dto.dashboard.DashboardResponse;
import com.jantabank.dto.dashboard.MiniStatementItemDto;
import com.jantabank.dto.dashboard.ModuleSummaryDto;
import com.jantabank.dto.dashboard.RecentBeneficiaryDto;
import com.jantabank.entity.Account;
import com.jantabank.entity.Beneficiary;
import com.jantabank.entity.Transaction;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.domain.enums.BeneficiaryStatus;
import com.jantabank.repository.BeneficiaryRepository;
import com.jantabank.repository.TransactionRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.AccountService;
import com.jantabank.service.DashboardService;
import com.jantabank.service.LoginHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private static final int MINI_STATEMENT_SIZE = 5;
    private static final int RECENT_BENEFICIARY_SIZE = 5;
    private static final String CURRENCY = "INR";

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final LoginHistoryService loginHistoryService;

    public DashboardServiceImpl(UserRepository userRepository,
                                AccountService accountService,
                                TransactionRepository transactionRepository,
                                BeneficiaryRepository beneficiaryRepository,
                                LoginHistoryService loginHistoryService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.loginHistoryService = loginHistoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Account> accounts = accountService.getAccountByUser(user.getId());
        Set<String> accountNumbers = accounts.stream()
                .map(Account::getAccountNumber)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<DashboardAccountDto> accountDtos = accounts.stream()
                .map(this::toAccountDto)
                .collect(Collectors.toList());

        double totalBalance = accounts.stream().mapToDouble(Account::getBalance).sum();

        List<MiniStatementItemDto> recentTransactions = buildMiniStatement(accountNumbers);
        List<RecentBeneficiaryDto> recentBeneficiaries = buildRecentBeneficiaries(accounts);
        int pendingBeneficiaries = countPendingBeneficiaries(accounts);

        LoginHistoryDto lastLogin = loginHistoryService.getLastSuccessfulLogin(user.getId());

        log.info("Dashboard assembled for userId={} accounts={} txns={}",
                user.getId(), accountDtos.size(), recentTransactions.size());

        return DashboardResponse.builder()
                .customerName(user.getName())
                .customerId(user.getId())
                .lastLoginTime(lastLogin != null ? lastLogin.getLoginTime() : null)
                .currency(CURRENCY)
                .totalBalance(totalBalance)
                .pendingBeneficiaryRequests(pendingBeneficiaries)
                .accounts(accountDtos)
                .recentTransactions(recentTransactions)
                .recentBeneficiaries(recentBeneficiaries)
                .cards(placeholder("Debit & Credit Cards"))
                .loans(placeholder("Loans"))
                .deposits(placeholder("Fixed & Recurring Deposits"))
                .build();
    }

    private List<MiniStatementItemDto> buildMiniStatement(Set<String> accountNumbers) {
        if (accountNumbers.isEmpty()) {
            return Collections.emptyList();
        }
        Pageable pageable = PageRequest.of(0, MINI_STATEMENT_SIZE);
        List<Transaction> transactions = transactionRepository.findByAccountNumbers(accountNumbers, pageable);
        List<MiniStatementItemDto> items = new ArrayList<>(transactions.size());
        for (Transaction t : transactions) {
            boolean debit = accountNumbers.contains(t.getFromaccount());
            String counterparty = debit ? t.getToaccount() : t.getFromaccount();
            items.add(MiniStatementItemDto.builder()
                    .id(t.getId())
                    .direction(debit ? "DEBIT" : "CREDIT")
                    .counterpartyAccount(maskAccount(counterparty))
                    .amount(t.getAmount())
                    .transactionDate(t.getTransactiondate())
                    .status(t.getStatus() != null ? t.getStatus().name() : null)
                    .build());
        }
        return items;
    }

    private List<RecentBeneficiaryDto> buildRecentBeneficiaries(List<Account> accounts) {
        return distinctBeneficiaries(accounts).stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .limit(RECENT_BENEFICIARY_SIZE)
                .map(b -> RecentBeneficiaryDto.builder()
                        .id(b.getId())
                        .name(b.getBeneficiaryaccountname())
                        .maskedAccountNumber(maskAccount(b.getBeneficiaryaccountnumber()))
                        .ifscCode(b.getBeneficiaryaccountifsc())
                        .status(b.getStatus() != null ? b.getStatus().name() : null)
                        .build())
                .collect(Collectors.toList());
    }

    private int countPendingBeneficiaries(List<Account> accounts) {
        return (int) distinctBeneficiaries(accounts).stream()
                .filter(b -> b.getStatus() == BeneficiaryStatus.PENDING)
                .count();
    }

    private Set<Beneficiary> distinctBeneficiaries(List<Account> accounts) {
        Set<Beneficiary> beneficiaries = new LinkedHashSet<>();
        for (Account account : accounts) {
            beneficiaries.addAll(beneficiaryRepository.findByAccounts_Id(account.getId()));
        }
        return beneficiaries;
    }

    private DashboardAccountDto toAccountDto(Account account) {
        return DashboardAccountDto.builder()
                .id(account.getId())
                .maskedAccountNumber(maskAccount(account.getAccountNumber()))
                .accountType(account.getAccountType())
                .ifscCode(account.getIfscCode())
                .status(account.getStatus() != null ? account.getStatus().name() : null)
                .balance(account.getBalance())
                .availableBalance(account.getBalance())
                .build();
    }

    private ModuleSummaryDto placeholder(String label) {
        return ModuleSummaryDto.builder()
                .label(label)
                .available(false)
                .count(0)
                .totalAmount(0d)
                .build();
    }

    private String maskAccount(String accountNumber) {
        if (accountNumber == null) {
            return null;
        }
        int len = accountNumber.length();
        if (len <= 4) {
            return accountNumber;
        }
        String last4 = accountNumber.substring(len - 4);
        return "XXXX" + last4;
    }
}
