package com.jantabank.impl;

import com.jantabank.domain.enums.TransactionStatus;
import com.jantabank.domain.enums.TransactionType;
import com.jantabank.domain.enums.TransferMode;
import com.jantabank.dto.txn.TransactionReceiptDto;
import com.jantabank.dto.txn.TransactionSummaryDto;
import com.jantabank.dto.txn.TransferRequest;
import com.jantabank.entity.Account;
import com.jantabank.entity.Transaction;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.AccountRepository;
import com.jantabank.repository.TransactionRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.domain.enums.NotificationChannel;
import com.jantabank.domain.enums.NotificationType;
import com.jantabank.service.MoneyTransferService;
import com.jantabank.service.NotificationService;
import com.lowagie.text.Document;
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
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MoneyTransferServiceImpl implements MoneyTransferService {

    private static final Logger log = LoggerFactory.getLogger(MoneyTransferServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_REFERENCE_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final double impsMaxAmount;
    private final double rtgsMinAmount;
    private final double neftMaxAmount;
    private final double perTransactionMaxAmount;
    private final double lowBalanceThreshold;

    public MoneyTransferServiceImpl(UserRepository userRepository,
                                    AccountRepository accountRepository,
                                    TransactionRepository transactionRepository,
                                    NotificationService notificationService,
                                    @Value("${app.transfer.imps-max-amount:500000}") double impsMaxAmount,
                                    @Value("${app.transfer.rtgs-min-amount:200000}") double rtgsMinAmount,
                                    @Value("${app.transfer.neft-max-amount:1000000}") double neftMaxAmount,
                                    @Value("${app.transfer.per-transaction-max-amount:2000000}") double perTransactionMaxAmount,
                                    @Value("${app.notification.low-balance-threshold:1000}") double lowBalanceThreshold) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
        this.impsMaxAmount = impsMaxAmount;
        this.rtgsMinAmount = rtgsMinAmount;
        this.neftMaxAmount = neftMaxAmount;
        this.perTransactionMaxAmount = perTransactionMaxAmount;
        this.lowBalanceThreshold = lowBalanceThreshold;
    }

    @Override
    @Transactional
    public TransactionReceiptDto transfer(TransferRequest request, String username) {
        User user = loadUser(username);
        TransactionType type = request.getTransferMode() == TransferMode.UPI
                ? TransactionType.UPI : TransactionType.TRANSFER;
        return executeTransfer(request, user, "WEB", type);
    }

    @Override
    @Transactional
    public TransactionReceiptDto executeTransfer(TransferRequest request, User user,
                                                 String channel, TransactionType transactionType) {
        double amount = request.getAmount();

        if (amount <= 0) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Source and destination accounts must differ");
        }
        validateModeLimits(request.getTransferMode(), amount);

        Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccountNumber());
        if (fromAccount == null) {
            throw new ResourceNotFoundException("Source account not found");
        }
        assertOwnership(fromAccount, user);

        if (fromAccount.getBalance() < amount) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Insufficient account balance");
        }

        // Destination may be internal (book transfer) or external (interbank rail).
        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber());
        if (toAccount == null && request.getTransferMode() == TransferMode.WITHIN_BANK) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "Destination account not found for within-bank transfer");
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        accountRepository.save(fromAccount);
        if (toAccount != null) {
            toAccount.setBalance(toAccount.getBalance() + amount);
            accountRepository.save(toAccount);
        }

        Transaction transaction = new Transaction();
        transaction.setFromaccount(request.getFromAccountNumber());
        transaction.setToaccount(request.getToAccountNumber());
        transaction.setAmount(amount);
        transaction.setTransactiondate(new Date());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setReferenceNumber(generateReference());
        transaction.setTransferMode(request.getTransferMode());
        transaction.setTransactionType(transactionType);
        transaction.setDescription(request.getDescription());
        transaction.setChannel(channel);
        transaction.setInitiatedByUserId(user.getId());

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transfer {} of {} via {} from {} to {} by userId={} channel={}",
                saved.getReferenceNumber(), amount, request.getTransferMode(),
                request.getFromAccountNumber(), request.getToAccountNumber(), user.getId(), channel);

        sendTransferNotifications(user, fromAccount, toAccount, amount, saved);

        return toReceipt(saved, ownedAccountNumbers(user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionSummaryDto> getHistory(String username, TransferMode mode, Double minAmount,
                                                  Double maxAmount, String query, LocalDate from, LocalDate to,
                                                  Pageable pageable) {
        User user = loadUser(username);
        Set<String> accountNumbers = ownedAccountNumbers(user.getId());
        if (accountNumbers.isEmpty()) {
            return Page.empty(pageable);
        }
        Date fromDate = startOfDay(from != null ? from : LocalDate.now().minusMonths(6));
        Date toDate = endOfDay(to != null ? to : LocalDate.now());
        String q = StringUtils.hasText(query) ? query.trim() : null;

        Page<Transaction> page = transactionRepository.search(
                accountNumbers, fromDate, toDate, mode, minAmount, maxAmount, q, ensureSort(pageable));
        return page.map(t -> toSummary(t, accountNumbers));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionReceiptDto getByReference(String referenceNumber, String username) {
        User user = loadUser(username);
        Set<String> accountNumbers = ownedAccountNumbers(user.getId());
        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        assertParty(transaction, accountNumbers, username);
        return toReceipt(transaction, accountNumbers);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateReceiptPdf(String referenceNumber, String username) {
        TransactionReceiptDto receipt = getByReference(referenceNumber, username);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Document document = new Document(PageSize.A5);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            document.add(new Paragraph("Transaction Receipt", titleFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addRow(table, "Reference", receipt.getReferenceNumber());
            addRow(table, "Status", receipt.getStatus());
            addRow(table, "Date", receipt.getTransactionDate() != null ? fmt.format(receipt.getTransactionDate()) : "-");
            addRow(table, "Mode", receipt.getTransferMode());
            addRow(table, "Type", receipt.getTransactionType());
            addRow(table, "From", receipt.getFromAccount());
            addRow(table, "To", receipt.getToAccount());
            addRow(table, "Direction", receipt.getDirection());
            addRow(table, "Amount", String.valueOf(receipt.getAmount()));
            addRow(table, "Description", receipt.getDescription() != null ? receipt.getDescription() : "-");
            document.add(table);
            document.close();
        } catch (Exception e) {
            log.error("Failed to generate receipt PDF for {}", referenceNumber, e);
            throw new TodoAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate receipt");
        }
        return out.toByteArray();
    }

    private void sendTransferNotifications(User user, Account fromAccount, Account toAccount,
                                           double amount, Transaction saved) {
        try {
            String txnDate = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(
                    saved.getTransactiondate() != null ? saved.getTransactiondate() : new Date());
            String debitMsg = String.format(
                    "Rs. %.2f debited from A/c %s on %s. Ref: %s. Avl Bal: Rs. %.2f.",
                    amount, maskAccount(fromAccount.getAccountNumber()), txnDate,
                    saved.getReferenceNumber(), fromAccount.getBalance());
            notificationService.notify(user.getId(), NotificationType.TRANSACTION_ALERT,
                    NotificationChannel.IN_APP, "Debit Alert", debitMsg);

            if (fromAccount.getBalance() < lowBalanceThreshold) {
                String lowMsg = String.format(
                        "The balance in your A/c %s is Rs. %.2f, which is below Rs. %.2f. Please maintain sufficient balance.",
                        maskAccount(fromAccount.getAccountNumber()), fromAccount.getBalance(), lowBalanceThreshold);
                notificationService.notify(user.getId(), NotificationType.LOW_BALANCE_ALERT,
                        NotificationChannel.IN_APP, "Low Balance Alert", lowMsg);
            }
        } catch (Exception ex) {
            log.warn("Failed to create transfer notification for txn {}: {}",
                    saved.getReferenceNumber(), ex.getMessage());
        }
    }

    private void validateModeLimits(TransferMode mode, double amount) {
        if (amount > perTransactionMaxAmount) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "Amount exceeds the per-transaction limit of " + perTransactionMaxAmount);
        }
        switch (mode) {
            case IMPS:
                if (amount > impsMaxAmount) {
                    throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                            "IMPS transfers are limited to " + impsMaxAmount);
                }
                break;
            case RTGS:
                if (amount < rtgsMinAmount) {
                    throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                            "RTGS requires a minimum amount of " + rtgsMinAmount);
                }
                break;
            case NEFT:
                if (amount > neftMaxAmount) {
                    throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                            "NEFT transfers are limited to " + neftMaxAmount);
                }
                break;
            default:
                // WITHIN_BANK and UPI only bound by the per-transaction limit.
                break;
        }
    }

    private void assertOwnership(Account account, User user) {
        boolean owns = account.getUsers() != null && account.getUsers().stream()
                .anyMatch(u -> u.getId() == user.getId());
        if (!owns) {
            log.warn("User '{}' attempted transfer from account {} they do not own",
                    user.getUsername(), account.getAccountNumber());
            throw new TodoAPIException(HttpStatus.FORBIDDEN, "You are not authorized to transfer from this account");
        }
    }

    private void assertParty(Transaction transaction, Set<String> accountNumbers, String username) {
        boolean party = accountNumbers.contains(transaction.getFromaccount())
                || accountNumbers.contains(transaction.getToaccount());
        if (!party) {
            log.warn("User '{}' attempted to view transaction {} they are not party to",
                    username, transaction.getReferenceNumber());
            throw new TodoAPIException(HttpStatus.FORBIDDEN, "You are not authorized to view this transaction");
        }
    }

    private Set<String> ownedAccountNumbers(long userId) {
        return accountRepository.findByUsers_Id(userId).stream()
                .map(Account::getAccountNumber)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String generateReference() {
        for (int i = 0; i < MAX_REFERENCE_ATTEMPTS; i++) {
            String candidate = "TXN" + System.currentTimeMillis() + String.format("%04d", RANDOM.nextInt(10000));
            if (transactionRepository.findByReferenceNumber(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new TodoAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not allocate a transaction reference");
    }

    private TransactionReceiptDto toReceipt(Transaction t, Set<String> accountNumbers) {
        boolean debit = accountNumbers.contains(t.getFromaccount());
        String counterparty = debit ? t.getToaccount() : t.getFromaccount();
        return TransactionReceiptDto.builder()
                .id(t.getId())
                .referenceNumber(t.getReferenceNumber())
                .fromAccount(maskAccount(t.getFromaccount()))
                .toAccount(maskAccount(t.getToaccount()))
                .direction(debit ? "DEBIT" : "CREDIT")
                .counterpartyAccount(maskAccount(counterparty))
                .amount(t.getAmount())
                .transferMode(t.getTransferMode() != null ? t.getTransferMode().name() : null)
                .transactionType(t.getTransactionType() != null ? t.getTransactionType().name() : null)
                .status(t.getStatus() != null ? t.getStatus().name() : null)
                .description(t.getDescription())
                .channel(t.getChannel())
                .transactionDate(t.getTransactiondate())
                .build();
    }

    private TransactionSummaryDto toSummary(Transaction t, Set<String> accountNumbers) {
        boolean debit = accountNumbers.contains(t.getFromaccount());
        String counterparty = debit ? t.getToaccount() : t.getFromaccount();
        return TransactionSummaryDto.builder()
                .id(t.getId())
                .referenceNumber(t.getReferenceNumber())
                .direction(debit ? "DEBIT" : "CREDIT")
                .counterpartyAccount(maskAccount(counterparty))
                .amount(t.getAmount())
                .transferMode(t.getTransferMode() != null ? t.getTransferMode().name() : null)
                .status(t.getStatus() != null ? t.getStatus().name() : null)
                .description(t.getDescription())
                .transactionDate(t.getTransactiondate())
                .build();
    }

    private void addRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        table.addCell(new PdfPCell(new Phrase(label, labelFont)));
        table.addCell(new PdfPCell(new Phrase(value != null ? value : "-", valueFont)));
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Pageable ensureSort(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "transactiondate"));
    }

    private Date startOfDay(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date endOfDay(LocalDate date) {
        return Date.from(date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
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
}
