package com.jantabank.impl;

import com.jantabank.domain.enums.DepositStatus;
import com.jantabank.domain.enums.DepositType;
import com.jantabank.dto.deposit.AutoRenewRequest;
import com.jantabank.dto.deposit.DepositCalculationRequest;
import com.jantabank.dto.deposit.DepositCalculationResponse;
import com.jantabank.dto.deposit.DepositResponse;
import com.jantabank.dto.deposit.OpenFixedDepositRequest;
import com.jantabank.dto.deposit.OpenRecurringDepositRequest;
import com.jantabank.entity.Account;
import com.jantabank.entity.Deposit;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.AccountRepository;
import com.jantabank.repository.DepositRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.DepositService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DepositServiceImpl implements DepositService {

    private static final Logger log = LoggerFactory.getLogger(DepositServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_REFERENCE_ATTEMPTS = 5;

    private final DepositRepository depositRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    private final double fdRate;
    private final double rdRate;
    private final int minTenure;
    private final int maxTenure;
    private final double minAmount;
    private final double penaltyRate;

    public DepositServiceImpl(DepositRepository depositRepository,
                              AccountRepository accountRepository,
                              UserRepository userRepository,
                              @Value("${app.deposit.fd-interest-rate:0.068}") double fdRate,
                              @Value("${app.deposit.rd-interest-rate:0.065}") double rdRate,
                              @Value("${app.deposit.min-tenure-months:3}") int minTenure,
                              @Value("${app.deposit.max-tenure-months:120}") int maxTenure,
                              @Value("${app.deposit.min-amount:1000}") double minAmount,
                              @Value("${app.deposit.premature-penalty-rate:0.01}") double penaltyRate) {
        this.depositRepository = depositRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.fdRate = fdRate;
        this.rdRate = rdRate;
        this.minTenure = minTenure;
        this.maxTenure = maxTenure;
        this.minAmount = minAmount;
        this.penaltyRate = penaltyRate;
    }

    @Override
    public DepositCalculationResponse calculate(DepositCalculationRequest request) {
        validateAmountAndTenure(request.getAmount(), request.getTenureMonths());
        boolean fixed = request.getDepositType() == DepositType.FIXED;
        double rate = fixed ? fdRate : rdRate;
        double maturity = fixed
                ? fdMaturity(request.getAmount(), rate, request.getTenureMonths())
                : rdMaturity(request.getAmount(), rate, request.getTenureMonths());
        double totalDeposited = fixed ? request.getAmount() : request.getAmount() * request.getTenureMonths();
        maturity = round2(maturity);
        return new DepositCalculationResponse(request.getDepositType(), request.getAmount(),
                request.getTenureMonths(), rate, round2(totalDeposited), maturity,
                round2(maturity - totalDeposited));
    }

    @Override
    @Transactional
    public DepositResponse openFixed(OpenFixedDepositRequest request, String username) {
        User user = loadUser(username);
        validateAmountAndTenure(request.getPrincipal(), request.getTenureMonths());
        Account account = loadOwnedAccount(request.getLinkedAccountNumber(), user);

        if (account.getBalance() < request.getPrincipal()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Insufficient balance to open the deposit");
        }
        account.setBalance(round2(account.getBalance() - request.getPrincipal()));
        accountRepository.save(account);

        Deposit deposit = new Deposit();
        deposit.setDepositReferenceNumber(generateReference());
        deposit.setUserId(user.getId());
        deposit.setLinkedAccountNumber(account.getAccountNumber());
        deposit.setDepositType(DepositType.FIXED);
        deposit.setPrincipal(round2(request.getPrincipal()));
        deposit.setInstallmentAmount(0.0);
        deposit.setInstallmentsPaid(0);
        deposit.setAnnualInterestRate(fdRate);
        deposit.setTenureMonths(request.getTenureMonths());
        deposit.setMaturityAmount(round2(fdMaturity(request.getPrincipal(), fdRate, request.getTenureMonths())));
        deposit.setStatus(DepositStatus.ACTIVE);
        deposit.setAutoRenew(request.isAutoRenew());
        deposit.setOpenedAt(LocalDateTime.now());
        deposit.setMaturityDate(LocalDate.now().plusMonths(request.getTenureMonths()));
        deposit = depositRepository.save(deposit);

        log.info("FD {} opened for user {} principal {} maturity {}",
                deposit.getDepositReferenceNumber(), user.getId(), deposit.getPrincipal(), deposit.getMaturityAmount());
        return toResponse(deposit);
    }

    @Override
    @Transactional
    public DepositResponse openRecurring(OpenRecurringDepositRequest request, String username) {
        User user = loadUser(username);
        validateAmountAndTenure(request.getMonthlyInstallment(), request.getTenureMonths());
        Account account = loadOwnedAccount(request.getLinkedAccountNumber(), user);

        if (account.getBalance() < request.getMonthlyInstallment()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Insufficient balance for the first installment");
        }
        account.setBalance(round2(account.getBalance() - request.getMonthlyInstallment()));
        accountRepository.save(account);

        Deposit deposit = new Deposit();
        deposit.setDepositReferenceNumber(generateReference());
        deposit.setUserId(user.getId());
        deposit.setLinkedAccountNumber(account.getAccountNumber());
        deposit.setDepositType(DepositType.RECURRING);
        deposit.setPrincipal(round2(request.getMonthlyInstallment()));
        deposit.setInstallmentAmount(round2(request.getMonthlyInstallment()));
        deposit.setInstallmentsPaid(1);
        deposit.setAnnualInterestRate(rdRate);
        deposit.setTenureMonths(request.getTenureMonths());
        deposit.setMaturityAmount(round2(rdMaturity(request.getMonthlyInstallment(), rdRate, request.getTenureMonths())));
        deposit.setStatus(DepositStatus.ACTIVE);
        deposit.setAutoRenew(request.isAutoRenew());
        deposit.setOpenedAt(LocalDateTime.now());
        deposit.setMaturityDate(LocalDate.now().plusMonths(request.getTenureMonths()));
        deposit = depositRepository.save(deposit);

        log.info("RD {} opened for user {} installment {} maturity {}",
                deposit.getDepositReferenceNumber(), user.getId(), deposit.getInstallmentAmount(), deposit.getMaturityAmount());
        return toResponse(deposit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositResponse> listMine(String username) {
        User user = loadUser(username);
        return depositRepository.findByUserIdOrderByIdDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepositResponse get(Long depositId, String username) {
        return toResponse(loadOwned(depositId, username));
    }

    @Override
    @Transactional
    public DepositResponse payInstallment(Long depositId, String username) {
        Deposit deposit = loadOwned(depositId, username);
        if (deposit.getDepositType() != DepositType.RECURRING) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Installments apply only to recurring deposits");
        }
        if (deposit.getStatus() != DepositStatus.ACTIVE) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Only active deposits accept installments");
        }
        if (deposit.getInstallmentsPaid() >= deposit.getTenureMonths()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "All installments have already been paid");
        }

        Account account = accountRepository.findByAccountNumber(deposit.getLinkedAccountNumber());
        if (account == null) {
            throw new ResourceNotFoundException("Linked account not found");
        }
        if (account.getBalance() < deposit.getInstallmentAmount()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Insufficient balance for the installment");
        }
        account.setBalance(round2(account.getBalance() - deposit.getInstallmentAmount()));
        accountRepository.save(account);

        deposit.setInstallmentsPaid(deposit.getInstallmentsPaid() + 1);
        deposit.setPrincipal(round2(deposit.getInstallmentAmount() * deposit.getInstallmentsPaid()));
        depositRepository.save(deposit);

        log.info("RD {} installment {}/{} paid", deposit.getDepositReferenceNumber(),
                deposit.getInstallmentsPaid(), deposit.getTenureMonths());
        return toResponse(deposit);
    }

    @Override
    @Transactional
    public DepositResponse close(Long depositId, String username) {
        Deposit deposit = loadOwned(depositId, username);
        if (deposit.getStatus() != DepositStatus.ACTIVE) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Only active deposits can be closed");
        }

        Account account = accountRepository.findByAccountNumber(deposit.getLinkedAccountNumber());
        if (account == null) {
            throw new ResourceNotFoundException("Linked account not found");
        }

        double payout = round2(prematurePayout(deposit));
        account.setBalance(round2(account.getBalance() + payout));
        accountRepository.save(account);

        deposit.setStatus(DepositStatus.CLOSED);
        deposit.setClosedAt(LocalDateTime.now());
        depositRepository.save(deposit);

        log.info("Deposit {} closed prematurely; payout {} to account {}",
                deposit.getDepositReferenceNumber(), payout, account.getAccountNumber());
        return toResponse(deposit);
    }

    @Override
    @Transactional
    public DepositResponse setAutoRenew(Long depositId, AutoRenewRequest request, String username) {
        Deposit deposit = loadOwned(depositId, username);
        if (deposit.getStatus() != DepositStatus.ACTIVE) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Auto-renewal can only be changed on active deposits");
        }
        deposit.setAutoRenew(request.isAutoRenew());
        depositRepository.save(deposit);
        return toResponse(deposit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> maturedDepositIds() {
        return depositRepository.findMaturedIds(DepositStatus.ACTIVE, LocalDate.now());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processMaturity(Long depositId) {
        Deposit deposit = depositRepository.findById(depositId).orElse(null);
        if (deposit == null || deposit.getStatus() != DepositStatus.ACTIVE
                || deposit.getMaturityDate().isAfter(LocalDate.now())) {
            return;
        }

        if (deposit.isAutoRenew()) {
            // Roll the matured corpus into a fresh fixed-term deposit.
            double corpus = round2(deposit.getMaturityAmount());
            deposit.setDepositType(DepositType.FIXED);
            deposit.setPrincipal(corpus);
            deposit.setInstallmentAmount(0.0);
            deposit.setInstallmentsPaid(0);
            deposit.setAnnualInterestRate(fdRate);
            deposit.setMaturityAmount(round2(fdMaturity(corpus, fdRate, deposit.getTenureMonths())));
            deposit.setOpenedAt(LocalDateTime.now());
            deposit.setMaturityDate(LocalDate.now().plusMonths(deposit.getTenureMonths()));
            depositRepository.save(deposit);
            log.info("Deposit {} auto-renewed; new corpus {} maturity {}",
                    deposit.getDepositReferenceNumber(), corpus, deposit.getMaturityAmount());
            return;
        }

        Account account = accountRepository.findByAccountNumber(deposit.getLinkedAccountNumber());
        if (account == null) {
            log.warn("Deposit {} matured but linked account {} missing; leaving active",
                    deposit.getDepositReferenceNumber(), deposit.getLinkedAccountNumber());
            return;
        }
        account.setBalance(round2(account.getBalance() + deposit.getMaturityAmount()));
        accountRepository.save(account);

        deposit.setStatus(DepositStatus.MATURED);
        deposit.setClosedAt(LocalDateTime.now());
        depositRepository.save(deposit);
        log.info("Deposit {} matured; credited {} to account {}",
                deposit.getDepositReferenceNumber(), deposit.getMaturityAmount(), account.getAccountNumber());
    }

    private double prematurePayout(Deposit deposit) {
        double deposited = deposit.getDepositType() == DepositType.FIXED
                ? deposit.getPrincipal()
                : deposit.getInstallmentAmount() * deposit.getInstallmentsPaid();
        long elapsedMonths = ChronoUnit.MONTHS.between(
                deposit.getOpenedAt().toLocalDate(), LocalDate.now());
        if (elapsedMonths < 0) {
            elapsedMonths = 0;
        }
        if (elapsedMonths > deposit.getTenureMonths()) {
            elapsedMonths = deposit.getTenureMonths();
        }
        double effectiveRate = Math.max(0.0, deposit.getAnnualInterestRate() - penaltyRate);
        double interest = deposited * effectiveRate * (elapsedMonths / 12.0);
        return deposited + interest;
    }

    private double fdMaturity(double principal, double annualRate, int tenureMonths) {
        double years = tenureMonths / 12.0;
        return principal * Math.pow(1 + annualRate / 4.0, 4 * years);
    }

    private double rdMaturity(double installment, double annualRate, int tenureMonths) {
        double monthlyRate = annualRate / 12.0;
        double maturity = 0.0;
        // Installment paid at the start of month k earns interest for (n-k+1) months.
        for (int k = 1; k <= tenureMonths; k++) {
            maturity += installment * Math.pow(1 + monthlyRate, tenureMonths - k + 1);
        }
        return maturity;
    }

    private void validateAmountAndTenure(double amount, int tenureMonths) {
        if (amount < minAmount) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Amount must be at least " + minAmount);
        }
        if (tenureMonths < minTenure || tenureMonths > maxTenure) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "Tenure must be between " + minTenure + " and " + maxTenure + " months");
        }
    }

    private String generateReference() {
        for (int i = 0; i < MAX_REFERENCE_ATTEMPTS; i++) {
            String candidate = "DP" + String.format("%012d", (long) (RANDOM.nextDouble() * 1_000_000_000_000L));
            if (!depositRepository.existsByDepositReferenceNumber(candidate)) {
                return candidate;
            }
        }
        throw new TodoAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not allocate a deposit reference");
    }

    private Account loadOwnedAccount(String accountNumber, User user) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new ResourceNotFoundException("Linked account not found");
        }
        boolean owns = account.getUsers() != null && account.getUsers().stream()
                .anyMatch(u -> u.getId() == user.getId());
        if (!owns) {
            throw new TodoAPIException(HttpStatus.FORBIDDEN, "You are not authorized to use this account");
        }
        return account;
    }

    private Deposit loadOwned(Long id, String username) {
        User user = loadUser(username);
        return depositRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private DepositResponse toResponse(Deposit d) {
        return DepositResponse.builder()
                .id(d.getId())
                .depositReferenceNumber(d.getDepositReferenceNumber())
                .depositType(d.getDepositType())
                .linkedAccountNumber(d.getLinkedAccountNumber())
                .principal(d.getPrincipal())
                .installmentAmount(d.getInstallmentAmount())
                .installmentsPaid(d.getInstallmentsPaid())
                .annualInterestRate(d.getAnnualInterestRate())
                .tenureMonths(d.getTenureMonths())
                .maturityAmount(d.getMaturityAmount())
                .status(d.getStatus())
                .autoRenew(d.isAutoRenew())
                .openedAt(d.getOpenedAt())
                .maturityDate(d.getMaturityDate())
                .closedAt(d.getClosedAt())
                .build();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
