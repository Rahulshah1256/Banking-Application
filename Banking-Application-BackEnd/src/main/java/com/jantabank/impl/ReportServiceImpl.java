package com.jantabank.impl;

import com.jantabank.domain.enums.CardStatus;
import com.jantabank.domain.enums.DepositStatus;
import com.jantabank.domain.enums.LoanStatus;
import com.jantabank.dto.report.AmountGroup;
import com.jantabank.dto.report.PortfolioReportResponse;
import com.jantabank.dto.report.TransactionReportResponse;
import com.jantabank.entity.Account;
import com.jantabank.entity.Card;
import com.jantabank.entity.Deposit;
import com.jantabank.entity.Loan;
import com.jantabank.entity.Transaction;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.repository.AccountRepository;
import com.jantabank.repository.CardRepository;
import com.jantabank.repository.DepositRepository;
import com.jantabank.repository.LoanRepository;
import com.jantabank.repository.TransactionRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;
    private final DepositRepository depositRepository;
    private final CardRepository cardRepository;

    public ReportServiceImpl(UserRepository userRepository,
                             AccountRepository accountRepository,
                             TransactionRepository transactionRepository,
                             LoanRepository loanRepository,
                             DepositRepository depositRepository,
                             CardRepository cardRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.loanRepository = loanRepository;
        this.depositRepository = depositRepository;
        this.cardRepository = cardRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionReportResponse myTransactionReport(String username, LocalDate from, LocalDate to) {
        User user = loadUser(username);
        LocalDate[] range = resolveRange(from, to);
        Set<String> accounts = accountRepository.findByUsers_Id(user.getId()).stream()
                .map(Account::getAccountNumber)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        List<Transaction> txns = accounts.isEmpty()
                ? List.of()
                : transactionRepository.findByAccountsInRange(accounts,
                        toStart(range[0]), toEndExclusive(range[1]));

        return buildReport("SELF", txns, accounts, range[0], range[1]);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioReportResponse myPortfolio(String username) {
        User user = loadUser(username);
        Long userId = user.getId();

        List<Account> accounts = accountRepository.findByUsers_Id(userId);
        List<PortfolioReportResponse.AccountHolding> holdings = accounts.stream()
                .map(a -> PortfolioReportResponse.AccountHolding.builder()
                        .maskedAccountNumber(maskAccount(a.getAccountNumber()))
                        .accountType(a.getAccountType())
                        .balance(round2(a.getBalance()))
                        .status(a.getStatus() == null ? null : a.getStatus().name())
                        .build())
                .toList();
        double totalBalance = round2(accounts.stream().mapToDouble(Account::getBalance).sum());

        List<Loan> loans = loanRepository.findByUserIdOrderByIdDesc(userId);
        double loanOutstanding = round2(loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .mapToDouble(Loan::getOutstandingPrincipal).sum());
        long activeLoans = loans.stream().filter(l -> l.getStatus() == LoanStatus.ACTIVE).count();

        List<Deposit> deposits = depositRepository.findByUserIdOrderByIdDesc(userId);
        long activeDeposits = deposits.stream().filter(d -> d.getStatus() == DepositStatus.ACTIVE).count();
        double depositPrincipal = round2(deposits.stream()
                .filter(d -> d.getStatus() == DepositStatus.ACTIVE)
                .mapToDouble(Deposit::getPrincipal).sum());
        double depositMaturity = round2(deposits.stream()
                .filter(d -> d.getStatus() == DepositStatus.ACTIVE)
                .mapToDouble(Deposit::getMaturityAmount).sum());

        List<Card> cards = cardRepository.findByUserIdOrderByIdDesc(userId);
        long activeCards = cards.stream().filter(c -> c.getStatus() == CardStatus.ACTIVE).count();

        double netWorth = round2(totalBalance + depositPrincipal - loanOutstanding);

        return PortfolioReportResponse.builder()
                .accountsCount(accounts.size())
                .totalBalance(totalBalance)
                .accounts(holdings)
                .loansCount(activeLoans)
                .totalLoanOutstanding(loanOutstanding)
                .depositsCount(activeDeposits)
                .totalDepositPrincipal(depositPrincipal)
                .totalDepositMaturityValue(depositMaturity)
                .cardsCount(cards.size())
                .activeCards(activeCards)
                .netWorth(netWorth)
                .build();
    }

    /**
     * Aggregates a set of transactions into a report. When {@code ownedAccounts} is provided the
     * debit/credit split is computed relative to those accounts; otherwise (bank-wide) it stays zero.
     */
    public static TransactionReportResponse buildReport(String scope, List<Transaction> txns,
                                                        Collection<String> ownedAccounts,
                                                        LocalDate from, LocalDate to) {
        double totalVolume = round2(txns.stream().mapToDouble(Transaction::getAmount).sum());
        double debit = 0;
        double credit = 0;
        if (ownedAccounts != null && !ownedAccounts.isEmpty()) {
            for (Transaction t : txns) {
                if (ownedAccounts.contains(t.getFromaccount())) {
                    debit += t.getAmount();
                }
                if (ownedAccounts.contains(t.getToaccount())) {
                    credit += t.getAmount();
                }
            }
        }
        return TransactionReportResponse.builder()
                .scope(scope)
                .fromDate(from)
                .toDate(to)
                .totalCount(txns.size())
                .totalVolume(totalVolume)
                .totalDebit(round2(debit))
                .totalCredit(round2(credit))
                .byMode(group(txns, t -> t.getTransferMode() == null ? null : t.getTransferMode().name()))
                .byType(group(txns, t -> t.getTransactionType() == null ? null : t.getTransactionType().name()))
                .byStatus(group(txns, t -> t.getStatus() == null ? null : t.getStatus().name()))
                .build();
    }

    private static List<AmountGroup> group(List<Transaction> txns, Function<Transaction, String> keyFn) {
        Map<String, AmountGroup> map = new LinkedHashMap<>();
        for (Transaction t : txns) {
            String key = keyFn.apply(t);
            if (key == null) {
                key = "UNKNOWN";
            }
            AmountGroup g = map.computeIfAbsent(key,
                    k -> AmountGroup.builder().key(k).count(0).volume(0).build());
            g.setCount(g.getCount() + 1);
            g.setVolume(round2(g.getVolume() + t.getAmount()));
        }
        return new ArrayList<>(map.values());
    }

    private LocalDate[] resolveRange(LocalDate from, LocalDate to) {
        LocalDate end = (to == null) ? LocalDate.now() : to;
        LocalDate start = (from == null) ? end.minusDays(30) : from;
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        return new LocalDate[]{start, end};
    }

    private Date toStart(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date toEndExclusive(LocalDate d) {
        return Date.from(d.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private String maskAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return accountNumber;
        }
        return "XXXX" + accountNumber.substring(accountNumber.length() - 4);
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
