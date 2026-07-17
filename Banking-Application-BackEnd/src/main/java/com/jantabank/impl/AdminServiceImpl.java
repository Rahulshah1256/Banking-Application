package com.jantabank.impl;

import com.jantabank.domain.enums.CardStatus;
import com.jantabank.domain.enums.DepositStatus;
import com.jantabank.domain.enums.KycStatus;
import com.jantabank.domain.enums.LoanStatus;
import com.jantabank.domain.enums.TicketStatus;
import com.jantabank.domain.enums.TransferMode;
import com.jantabank.domain.enums.UserStatus;
import com.jantabank.dto.admin.AdminAccountView;
import com.jantabank.dto.admin.AdminOverviewResponse;
import com.jantabank.dto.admin.AdminTransactionView;
import com.jantabank.dto.admin.AdminUserDetail;
import com.jantabank.dto.admin.AdminUserSummary;
import com.jantabank.dto.report.TransactionReportResponse;
import com.jantabank.entity.Account;
import com.jantabank.entity.Role;
import com.jantabank.entity.Transaction;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.AccountRepository;
import com.jantabank.repository.CardRepository;
import com.jantabank.repository.DepositRepository;
import com.jantabank.repository.KycDocumentRepository;
import com.jantabank.repository.LoanRepository;
import com.jantabank.repository.SupportTicketRepository;
import com.jantabank.repository.TransactionRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;
    private final DepositRepository depositRepository;
    private final CardRepository cardRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final KycDocumentRepository kycDocumentRepository;

    public AdminServiceImpl(UserRepository userRepository,
                            AccountRepository accountRepository,
                            TransactionRepository transactionRepository,
                            LoanRepository loanRepository,
                            DepositRepository depositRepository,
                            CardRepository cardRepository,
                            SupportTicketRepository supportTicketRepository,
                            KycDocumentRepository kycDocumentRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.loanRepository = loanRepository;
        this.depositRepository = depositRepository;
        this.cardRepository = cardRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.kycDocumentRepository = kycDocumentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOverviewResponse overview() {
        Map<String, Long> usersByStatus = new LinkedHashMap<>();
        for (UserStatus s : UserStatus.values()) {
            usersByStatus.put(s.name(), userRepository.countByStatus(s));
        }
        long totalUsers = usersByStatus.values().stream().mapToLong(Long::longValue).sum();

        Map<String, Long> cardsByStatus = new LinkedHashMap<>();
        for (CardStatus s : CardStatus.values()) {
            cardsByStatus.put(s.name(), cardRepository.countByStatus(s));
        }

        return AdminOverviewResponse.builder()
                .totalUsers(totalUsers)
                .usersByStatus(usersByStatus)
                .totalAccounts(accountRepository.count())
                .totalBalance(round2(accountRepository.sumAllBalances()))
                .totalTransactions(transactionRepository.count())
                .totalTransactionVolume(round2(transactionRepository.sumAllAmounts()))
                .activeLoans(loanRepository.countByStatus(LoanStatus.ACTIVE))
                .totalLoanOutstanding(round2(loanRepository.sumOutstandingByStatus(LoanStatus.ACTIVE)))
                .activeDeposits(depositRepository.countByStatus(DepositStatus.ACTIVE))
                .totalDepositPrincipal(round2(depositRepository.sumPrincipalByStatus(DepositStatus.ACTIVE)))
                .totalCards(cardRepository.count())
                .cardsByStatus(cardsByStatus)
                .openSupportTickets(supportTicketRepository.countByStatus(TicketStatus.OPEN))
                .pendingKycDocuments(kycDocumentRepository.countByStatus(KycStatus.PENDING))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserSummary> listUsers(UserStatus status, String query, Pageable pageable) {
        String q = (query == null) ? null : query.trim().toLowerCase();
        List<AdminUserSummary> all = userRepository.findAll().stream()
                .filter(u -> status == null || u.getStatus() == status)
                .filter(u -> q == null || q.isEmpty()
                        || contains(u.getName(), q)
                        || contains(u.getUsername(), q)
                        || contains(u.getEmail(), q))
                .sorted(Comparator.comparingLong(User::getId))
                .map(this::toSummary)
                .toList();
        return manualPage(all, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetail getUserDetail(Long userId) {
        User user = userRepository.findById(userId.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Account> accounts = accountRepository.findByUsers_Id(user.getId());
        List<AdminAccountView> accountViews = accounts.stream()
                .map(a -> AdminAccountView.builder()
                        .maskedAccountNumber(maskTail(a.getAccountNumber(), 4))
                        .accountType(a.getAccountType())
                        .balance(round2(a.getBalance()))
                        .status(a.getStatus() == null ? null : a.getStatus().name())
                        .build())
                .toList();
        double totalBalance = round2(accounts.stream().mapToDouble(Account::getBalance).sum());

        return AdminUserDetail.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .maskedEmail(maskEmail(user.getEmail()))
                .maskedMobile(maskTail(user.getMobile(), 4))
                .maskedPan(maskTail(user.getPanNo(), 4))
                .maskedAadhaar(maskTail(user.getAadhaarNo(), 4))
                .address(user.getAddress())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .roles(roleNames(user))
                .accounts(accountViews)
                .totalBalance(totalBalance)
                .loansCount(loanRepository.countByUserId(user.getId()))
                .depositsCount(depositRepository.countByUserId(user.getId()))
                .cardsCount(cardRepository.countByUserId(user.getId()))
                .kycDocumentsCount(kycDocumentRepository.countByUserId(user.getId()))
                .supportTicketsCount(supportTicketRepository.countByUserId(user.getId()))
                .build();
    }

    @Override
    @Transactional
    public AdminUserSummary updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (roleNames(user).contains("ROLE_ADMIN") && status != UserStatus.ACTIVE) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Administrator accounts cannot be deactivated");
        }
        user.setStatus(status);
        User saved = userRepository.save(user);
        log.info("Admin updated user {} status to {}", saved.getId(), status);
        return toSummary(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminTransactionView> transactions(LocalDate from, LocalDate to, TransferMode mode, Pageable pageable) {
        LocalDate end = (to == null) ? LocalDate.now() : to;
        LocalDate start = (from == null) ? end.minusDays(90) : from;
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        List<AdminTransactionView> all = transactionRepository
                .findAllInRange(toStart(start), toEndExclusive(end)).stream()
                .filter(t -> mode == null || t.getTransferMode() == mode)
                .sorted(Comparator.comparing(Transaction::getTransactiondate,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toTransactionView)
                .toList();
        return manualPage(all, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionReportResponse transactionReport(LocalDate from, LocalDate to) {
        LocalDate end = (to == null) ? LocalDate.now() : to;
        LocalDate start = (from == null) ? end.minusDays(30) : from;
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        List<Transaction> txns = transactionRepository.findAllInRange(toStart(start), toEndExclusive(end));
        return ReportServiceImpl.buildReport("BANK_WIDE", txns, null, start, end);
    }

    private AdminUserSummary toSummary(User user) {
        return AdminUserSummary.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .maskedEmail(maskEmail(user.getEmail()))
                .maskedMobile(maskTail(user.getMobile(), 4))
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .roles(roleNames(user))
                .accountsCount(accountRepository.findByUsers_Id(user.getId()).size())
                .build();
    }

    private AdminTransactionView toTransactionView(Transaction t) {
        return AdminTransactionView.builder()
                .id(t.getId())
                .referenceNumber(t.getReferenceNumber())
                .maskedFromAccount(maskTail(t.getFromaccount(), 4))
                .maskedToAccount(maskTail(t.getToaccount(), 4))
                .amount(round2(t.getAmount()))
                .transactionDate(t.getTransactiondate())
                .transferMode(t.getTransferMode())
                .transactionType(t.getTransactionType())
                .status(t.getStatus())
                .channel(t.getChannel())
                .initiatedByUserId(t.getInitiatedByUserId())
                .build();
    }

    private List<String> roleNames(User user) {
        Set<Role> roles = user.getRoles();
        if (roles == null) {
            return List.of();
        }
        return roles.stream().map(Role::getName).collect(Collectors.toList());
    }

    private <T> Page<T> manualPage(List<T> all, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= all.size()) {
            return new PageImpl<>(List.of(), pageable, all.size());
        }
        int end = Math.min(start + pageable.getPageSize(), all.size());
        return new PageImpl<>(all.subList(start, end), pageable, all.size());
    }

    private boolean contains(String value, String lowerNeedle) {
        return value != null && value.toLowerCase().contains(lowerNeedle);
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

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return maskTail(email, 2);
        }
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String maskedLocal = local.length() <= 2
                ? local.charAt(0) + "*"
                : local.substring(0, 2) + "****";
        return maskedLocal + "@" + parts[1];
    }

    private String maskTail(String value, int visible) {
        if (value == null || value.length() <= visible) {
            return value;
        }
        int hidden = value.length() - visible;
        return "*".repeat(Math.min(hidden, 4)) + value.substring(hidden);
    }
}
