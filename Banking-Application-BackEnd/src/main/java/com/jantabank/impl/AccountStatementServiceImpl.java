package com.jantabank.impl;

import com.jantabank.dto.account.AccountDetailsDto;
import com.jantabank.dto.account.AccountSummaryDto;
import com.jantabank.dto.account.StatementItemDto;
import com.jantabank.entity.Account;
import com.jantabank.entity.Transaction;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.AccountRepository;
import com.jantabank.repository.TransactionRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.AccountStatementService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class AccountStatementServiceImpl implements AccountStatementService {

    private static final Logger log = LoggerFactory.getLogger(AccountStatementServiceImpl.class);
    private static final int EXPORT_MAX_ROWS = 10_000;

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final double savingsRate;
    private final double currentRate;
    private final double defaultRate;

    public AccountStatementServiceImpl(AccountRepository accountRepository,
                                       UserRepository userRepository,
                                       TransactionRepository transactionRepository,
                                       @Value("${app.account.savings-interest-rate:0.035}") double savingsRate,
                                       @Value("${app.account.current-interest-rate:0.0}") double currentRate,
                                       @Value("${app.account.default-interest-rate:0.03}") double defaultRate) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.savingsRate = savingsRate;
        this.currentRate = currentRate;
        this.defaultRate = defaultRate;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDetailsDto getDetails(long accountId, String username, boolean admin) {
        Account account = loadOwnedAccount(accountId, username, admin);
        return AccountDetailsDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountHolderName(account.getAccountHolderName())
                .accountType(account.getAccountType())
                .branchId(account.getBranchId())
                .ifscCode(account.getIfscCode())
                .status(account.getStatus() != null ? account.getStatus().name() : null)
                .openDate(account.getOpenDate())
                .address(account.getAddress())
                .contactNumber(account.getContactNumber())
                .emailAddress(account.getEmailAddress())
                .nominee(account.getNominee())
                .balance(account.getBalance())
                .availableBalance(account.getBalance())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountSummaryDto getSummary(long accountId, String username, boolean admin) {
        Account account = loadOwnedAccount(accountId, username, admin);
        User caller = loadUser(username);
        double rate = rateFor(account.getAccountType());
        long ageDays = account.getOpenDate() != null
                ? ChronoUnit.DAYS.between(account.getOpenDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now())
                : 0;
        return AccountSummaryDto.builder()
                .id(account.getId())
                .maskedAccountNumber(maskAccount(account.getAccountNumber()))
                .accountType(account.getAccountType())
                .status(account.getStatus() != null ? account.getStatus().name() : null)
                .currentBalance(account.getBalance())
                .availableBalance(account.getBalance())
                .accountAgeDays(Math.max(ageDays, 0))
                .annualInterestRate(rate)
                .projectedAnnualInterest(round2(account.getBalance() * rate))
                .nomineeName(account.getNominee())
                .kycStatus(deriveKycStatus(caller))
                .chequeBookStatus("NOT_REQUESTED")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StatementItemDto> getStatement(long accountId, String username, boolean admin,
                                               LocalDate from, LocalDate to, Pageable pageable) {
        Account account = loadOwnedAccount(accountId, username, admin);
        Date fromDate = startOfDay(defaultFrom(from));
        Date toDate = endOfDay(defaultTo(to));
        Pageable sorted = ensureSort(pageable);
        Page<Transaction> page = transactionRepository.findStatement(account.getAccountNumber(), fromDate, toDate, sorted);
        return page.map(t -> toStatementItem(t, account.getAccountNumber()));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv(long accountId, String username, boolean admin, LocalDate from, LocalDate to) {
        Account account = loadOwnedAccount(accountId, username, admin);
        List<StatementItemDto> items = fetchAllForExport(account, from, to);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("Statement for,").append(maskAccount(account.getAccountNumber())).append('\n');
        sb.append("Period,").append(defaultFrom(from)).append(" to ").append(defaultTo(to)).append('\n');
        sb.append("Date,Direction,Counterparty,Amount,Status\n");
        for (StatementItemDto item : items) {
            sb.append(fmt.format(item.getValueDate())).append(',')
              .append(item.getDirection()).append(',')
              .append(item.getCounterpartyAccount()).append(',')
              .append(item.getAmount()).append(',')
              .append(item.getStatus()).append('\n');
        }
        log.info("CSV statement exported for accountId={} rows={}", accountId, items.size());
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportPdf(long accountId, String username, boolean admin, LocalDate from, LocalDate to) {
        Account account = loadOwnedAccount(accountId, username, admin);
        List<StatementItemDto> items = fetchAllForExport(account, from, to);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            document.add(new Paragraph("Account Statement", titleFont));
            document.add(new Paragraph("Account: " + maskAccount(account.getAccountNumber()), metaFont));
            document.add(new Paragraph("Holder: " + account.getAccountHolderName(), metaFont));
            document.add(new Paragraph("Period: " + defaultFrom(from) + " to " + defaultTo(to), metaFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(new float[]{3, 2, 3, 2, 2});
            table.setWidthPercentage(100);
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            for (String header : new String[]{"Date", "Direction", "Counterparty", "Amount", "Status"}) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(cell);
            }
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (StatementItemDto item : items) {
                table.addCell(new Phrase(fmt.format(item.getValueDate()), cellFont));
                table.addCell(new Phrase(item.getDirection(), cellFont));
                table.addCell(new Phrase(item.getCounterpartyAccount(), cellFont));
                table.addCell(new Phrase(String.valueOf(item.getAmount()), cellFont));
                table.addCell(new Phrase(item.getStatus(), cellFont));
            }
            if (items.isEmpty()) {
                document.add(new Paragraph("No transactions in the selected period.", metaFont));
            } else {
                document.add(table);
            }
            document.close();
        } catch (Exception e) {
            log.error("Failed to generate PDF statement for accountId={}", accountId, e);
            throw new TodoAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate PDF statement");
        }
        log.info("PDF statement exported for accountId={} rows={}", accountId, items.size());
        return out.toByteArray();
    }

    private List<StatementItemDto> fetchAllForExport(Account account, LocalDate from, LocalDate to) {
        Date fromDate = startOfDay(defaultFrom(from));
        Date toDate = endOfDay(defaultTo(to));
        Pageable pageable = PageRequest.of(0, EXPORT_MAX_ROWS, Sort.by(Sort.Direction.DESC, "transactiondate"));
        return transactionRepository.findStatement(account.getAccountNumber(), fromDate, toDate, pageable)
                .map(t -> toStatementItem(t, account.getAccountNumber()))
                .getContent();
    }

    private StatementItemDto toStatementItem(Transaction t, String accountNumber) {
        boolean debit = accountNumber.equals(t.getFromaccount());
        String counterparty = debit ? t.getToaccount() : t.getFromaccount();
        return StatementItemDto.builder()
                .id(t.getId())
                .valueDate(t.getTransactiondate())
                .direction(debit ? "DEBIT" : "CREDIT")
                .counterpartyAccount(maskAccount(counterparty))
                .amount(t.getAmount())
                .status(t.getStatus() != null ? t.getStatus().name() : null)
                .build();
    }

    private Account loadOwnedAccount(long accountId, String username, boolean admin) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id : " + accountId));
        if (admin) {
            return account;
        }
        User caller = loadUser(username);
        boolean owns = account.getUsers() != null && account.getUsers().stream()
                .anyMatch(u -> u.getId() == caller.getId());
        if (!owns) {
            log.warn("User '{}' attempted to access account {} they do not own", username, accountId);
            throw new TodoAPIException(HttpStatus.FORBIDDEN, "You are not authorized to access this account");
        }
        return account;
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String deriveKycStatus(User user) {
        boolean hasPan = StringUtils.hasText(user.getPanNo());
        boolean hasAadhaar = StringUtils.hasText(user.getAadhaarNo());
        return (hasPan && hasAadhaar) ? "VERIFIED" : "PENDING";
    }

    private double rateFor(String accountType) {
        if (accountType == null) {
            return defaultRate;
        }
        switch (accountType.trim().toUpperCase()) {
            case "SAVINGS":
                return savingsRate;
            case "CURRENT":
                return currentRate;
            default:
                return defaultRate;
        }
    }

    private LocalDate defaultFrom(LocalDate from) {
        return from != null ? from : LocalDate.now().minusMonths(6);
    }

    private LocalDate defaultTo(LocalDate to) {
        return to != null ? to : LocalDate.now();
    }

    private Date startOfDay(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date endOfDay(LocalDate date) {
        return Date.from(date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
    }

    private Pageable ensureSort(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "transactiondate"));
    }

    private String maskAccount(String accountNumber) {
        if (accountNumber == null) {
            return null;
        }
        int len = accountNumber.length();
        if (len <= 4) {
            return accountNumber;
        }
        return "XXXX" + accountNumber.substring(len - 4);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
