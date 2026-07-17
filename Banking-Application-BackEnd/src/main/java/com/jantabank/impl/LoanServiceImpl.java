package com.jantabank.impl;

import com.jantabank.domain.enums.LoanStatus;
import com.jantabank.domain.enums.LoanType;
import com.jantabank.domain.enums.RepaymentType;
import com.jantabank.dto.loan.AmortizationEntry;
import com.jantabank.dto.loan.LoanApplicationRequest;
import com.jantabank.dto.loan.LoanCalculationRequest;
import com.jantabank.dto.loan.LoanCalculationResponse;
import com.jantabank.dto.loan.LoanPrepaymentRequest;
import com.jantabank.dto.loan.LoanRepaymentResponse;
import com.jantabank.dto.loan.LoanResponse;
import com.jantabank.entity.Account;
import com.jantabank.entity.Loan;
import com.jantabank.entity.LoanRepayment;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.AccountRepository;
import com.jantabank.repository.LoanRepaymentRepository;
import com.jantabank.repository.LoanRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.LoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanServiceImpl implements LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_REFERENCE_ATTEMPTS = 5;

    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    private final double homeRate;
    private final double carRate;
    private final double educationRate;
    private final double personalRate;
    private final double defaultRate;
    private final int minTenure;
    private final int maxTenure;
    private final double minPrincipal;
    private final double maxPrincipal;

    public LoanServiceImpl(LoanRepository loanRepository,
                           LoanRepaymentRepository loanRepaymentRepository,
                           AccountRepository accountRepository,
                           UserRepository userRepository,
                           @Value("${app.loan.home-interest-rate:0.085}") double homeRate,
                           @Value("${app.loan.car-interest-rate:0.095}") double carRate,
                           @Value("${app.loan.education-interest-rate:0.105}") double educationRate,
                           @Value("${app.loan.personal-interest-rate:0.14}") double personalRate,
                           @Value("${app.loan.default-interest-rate:0.12}") double defaultRate,
                           @Value("${app.loan.min-tenure-months:3}") int minTenure,
                           @Value("${app.loan.max-tenure-months:360}") int maxTenure,
                           @Value("${app.loan.min-principal:10000}") double minPrincipal,
                           @Value("${app.loan.max-principal:50000000}") double maxPrincipal) {
        this.loanRepository = loanRepository;
        this.loanRepaymentRepository = loanRepaymentRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.homeRate = homeRate;
        this.carRate = carRate;
        this.educationRate = educationRate;
        this.personalRate = personalRate;
        this.defaultRate = defaultRate;
        this.minTenure = minTenure;
        this.maxTenure = maxTenure;
        this.minPrincipal = minPrincipal;
        this.maxPrincipal = maxPrincipal;
    }

    @Override
    public LoanCalculationResponse calculate(LoanCalculationRequest request) {
        validatePrincipalAndTenure(request.getPrincipal(), request.getTenureMonths());
        double rate = rateFor(request.getLoanType());
        double emi = round2(computeEmi(request.getPrincipal(), rate, request.getTenureMonths()));
        double totalPayable = round2(emi * request.getTenureMonths());
        double totalInterest = round2(totalPayable - request.getPrincipal());
        return new LoanCalculationResponse(request.getLoanType(), request.getPrincipal(), rate,
                request.getTenureMonths(), emi, totalPayable, totalInterest);
    }

    @Override
    @Transactional
    public LoanResponse apply(LoanApplicationRequest request, String username) {
        User user = loadUser(username);
        validatePrincipalAndTenure(request.getPrincipal(), request.getTenureMonths());

        Account account = accountRepository.findByAccountNumber(request.getDisbursementAccountNumber());
        if (account == null) {
            throw new ResourceNotFoundException("Disbursement account not found");
        }
        assertOwnership(account, user);

        double rate = rateFor(request.getLoanType());
        double emi = round2(computeEmi(request.getPrincipal(), rate, request.getTenureMonths()));

        Loan loan = new Loan();
        loan.setLoanReferenceNumber(generateReference());
        loan.setUserId(user.getId());
        loan.setDisbursementAccountNumber(account.getAccountNumber());
        loan.setLoanType(request.getLoanType());
        loan.setPrincipal(round2(request.getPrincipal()));
        loan.setAnnualInterestRate(rate);
        loan.setTenureMonths(request.getTenureMonths());
        loan.setEmiAmount(emi);
        loan.setOutstandingPrincipal(round2(request.getPrincipal()));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setEmisPaid(0);
        loan.setNextEmiDate(LocalDate.now().plusMonths(1));
        loan.setAppliedAt(LocalDateTime.now());
        loan.setDisbursedAt(LocalDateTime.now());
        loan = loanRepository.save(loan);

        account.setBalance(round2(account.getBalance() + request.getPrincipal()));
        accountRepository.save(account);

        log.info("Loan {} disbursed to account {} for user {} amount {}",
                loan.getLoanReferenceNumber(), account.getAccountNumber(), user.getId(), request.getPrincipal());
        return toResponse(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> listMine(String username) {
        User user = loadUser(username);
        return loanRepository.findByUserIdOrderByIdDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponse get(Long loanId, String username) {
        return toResponse(loadOwned(loanId, username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmortizationEntry> schedule(Long loanId, String username) {
        Loan loan = loadOwned(loanId, username);
        return projectSchedule(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanRepaymentResponse> statement(Long loanId, String username) {
        Loan loan = loadOwned(loanId, username);
        return loanRepaymentRepository.findByLoanIdOrderByIdAsc(loan.getId()).stream()
                .map(this::toRepaymentResponse)
                .toList();
    }

    @Override
    @Transactional
    public LoanResponse payEmi(Long loanId, String username) {
        Loan loan = loadOwned(loanId, username);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Only active loans can be repaid");
        }

        Account account = accountRepository.findByAccountNumber(loan.getDisbursementAccountNumber());
        if (account == null) {
            throw new ResourceNotFoundException("Linked account not found");
        }

        double monthlyRate = loan.getAnnualInterestRate() / 12.0;
        double interest = round2(loan.getOutstandingPrincipal() * monthlyRate);
        double principalComponent = round2(loan.getEmiAmount() - interest);
        if (principalComponent > loan.getOutstandingPrincipal()) {
            principalComponent = loan.getOutstandingPrincipal();
        }
        double due = round2(principalComponent + interest);

        if (account.getBalance() < due) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Insufficient balance to pay EMI");
        }

        account.setBalance(round2(account.getBalance() - due));
        accountRepository.save(account);

        double outstandingAfter = round2(loan.getOutstandingPrincipal() - principalComponent);
        loan.setOutstandingPrincipal(outstandingAfter);
        loan.setEmisPaid(loan.getEmisPaid() + 1);
        if (outstandingAfter <= 0.0 || loan.getEmisPaid() >= loan.getTenureMonths()) {
            closeLoan(loan);
        } else {
            loan.setNextEmiDate(LocalDate.now().plusMonths(1));
        }
        loanRepository.save(loan);

        recordRepayment(loan, RepaymentType.EMI, due, principalComponent, interest, loan.getOutstandingPrincipal());
        log.info("EMI paid for loan {} principal={} interest={} outstanding={}",
                loan.getLoanReferenceNumber(), principalComponent, interest, loan.getOutstandingPrincipal());
        return toResponse(loan);
    }

    @Override
    @Transactional
    public LoanResponse prepay(Long loanId, LoanPrepaymentRequest request, String username) {
        Loan loan = loadOwned(loanId, username);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Only active loans can be prepaid");
        }

        double amount = round2(request.getAmount());
        if (amount > loan.getOutstandingPrincipal()) {
            amount = loan.getOutstandingPrincipal();
        }

        Account account = accountRepository.findByAccountNumber(loan.getDisbursementAccountNumber());
        if (account == null) {
            throw new ResourceNotFoundException("Linked account not found");
        }
        if (account.getBalance() < amount) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Insufficient balance for prepayment");
        }

        account.setBalance(round2(account.getBalance() - amount));
        accountRepository.save(account);

        double outstandingAfter = round2(loan.getOutstandingPrincipal() - amount);
        loan.setOutstandingPrincipal(outstandingAfter);
        if (outstandingAfter <= 0.0) {
            closeLoan(loan);
        }
        loanRepository.save(loan);

        recordRepayment(loan, RepaymentType.PREPAYMENT, amount, amount, 0.0, loan.getOutstandingPrincipal());
        log.info("Prepayment of {} on loan {} outstanding now {}",
                amount, loan.getLoanReferenceNumber(), loan.getOutstandingPrincipal());
        return toResponse(loan);
    }

    private void closeLoan(Loan loan) {
        loan.setOutstandingPrincipal(0.0);
        loan.setStatus(LoanStatus.CLOSED);
        loan.setNextEmiDate(null);
    }

    private List<AmortizationEntry> projectSchedule(Loan loan) {
        List<AmortizationEntry> entries = new ArrayList<>();
        double balance = loan.getOutstandingPrincipal();
        double monthlyRate = loan.getAnnualInterestRate() / 12.0;
        double emi = loan.getEmiAmount();
        LocalDate due = loan.getNextEmiDate() != null ? loan.getNextEmiDate() : LocalDate.now().plusMonths(1);
        int remaining = loan.getTenureMonths() - loan.getEmisPaid();
        int installment = loan.getEmisPaid();

        for (int i = 0; i < remaining && balance > 0.0; i++) {
            installment++;
            double interest = round2(balance * monthlyRate);
            double principal = round2(emi - interest);
            if (principal > balance) {
                principal = balance;
            }
            balance = round2(balance - principal);
            entries.add(AmortizationEntry.builder()
                    .installmentNumber(installment)
                    .dueDate(due)
                    .emiAmount(round2(principal + interest))
                    .principalComponent(principal)
                    .interestComponent(interest)
                    .balanceAfter(balance)
                    .build());
            due = due.plusMonths(1);
        }
        return entries;
    }

    private double computeEmi(double principal, double annualRate, int tenureMonths) {
        double monthlyRate = annualRate / 12.0;
        if (monthlyRate <= 0.0) {
            return principal / tenureMonths;
        }
        double pow = Math.pow(1 + monthlyRate, tenureMonths);
        return principal * monthlyRate * pow / (pow - 1);
    }

    private double rateFor(LoanType type) {
        return switch (type) {
            case HOME -> homeRate;
            case CAR -> carRate;
            case EDUCATION -> educationRate;
            case PERSONAL -> personalRate;
        };
    }

    private void validatePrincipalAndTenure(double principal, int tenureMonths) {
        if (principal < minPrincipal || principal > maxPrincipal) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "Principal must be between " + minPrincipal + " and " + maxPrincipal);
        }
        if (tenureMonths < minTenure || tenureMonths > maxTenure) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "Tenure must be between " + minTenure + " and " + maxTenure + " months");
        }
    }

    private void recordRepayment(Loan loan, RepaymentType type, double amount,
                                 double principalComponent, double interestComponent, double outstandingAfter) {
        LoanRepayment repayment = new LoanRepayment();
        repayment.setLoanId(loan.getId());
        repayment.setRepaymentType(type);
        repayment.setAmount(amount);
        repayment.setPrincipalComponent(principalComponent);
        repayment.setInterestComponent(interestComponent);
        repayment.setOutstandingAfter(outstandingAfter);
        repayment.setPaidAt(LocalDateTime.now());
        loanRepaymentRepository.save(repayment);
    }

    private String generateReference() {
        for (int i = 0; i < MAX_REFERENCE_ATTEMPTS; i++) {
            String candidate = "LN" + String.format("%012d", (long) (RANDOM.nextDouble() * 1_000_000_000_000L));
            if (!loanRepository.existsByLoanReferenceNumber(candidate)) {
                return candidate;
            }
        }
        throw new TodoAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not allocate a loan reference");
    }

    private void assertOwnership(Account account, User user) {
        boolean owns = account.getUsers() != null && account.getUsers().stream()
                .anyMatch(u -> u.getId() == user.getId());
        if (!owns) {
            throw new TodoAPIException(HttpStatus.FORBIDDEN, "You are not authorized to use this account");
        }
    }

    private Loan loadOwned(Long id, String username) {
        User user = loadUser(username);
        return loanRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private LoanResponse toResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .loanReferenceNumber(loan.getLoanReferenceNumber())
                .loanType(loan.getLoanType())
                .disbursementAccountNumber(loan.getDisbursementAccountNumber())
                .principal(loan.getPrincipal())
                .annualInterestRate(loan.getAnnualInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .emiAmount(loan.getEmiAmount())
                .outstandingPrincipal(loan.getOutstandingPrincipal())
                .status(loan.getStatus())
                .emisPaid(loan.getEmisPaid())
                .nextEmiDate(loan.getNextEmiDate())
                .appliedAt(loan.getAppliedAt())
                .disbursedAt(loan.getDisbursedAt())
                .build();
    }

    private LoanRepaymentResponse toRepaymentResponse(LoanRepayment r) {
        return LoanRepaymentResponse.builder()
                .id(r.getId())
                .repaymentType(r.getRepaymentType())
                .amount(r.getAmount())
                .principalComponent(r.getPrincipalComponent())
                .interestComponent(r.getInterestComponent())
                .outstandingAfter(r.getOutstandingAfter())
                .paidAt(r.getPaidAt())
                .build();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
