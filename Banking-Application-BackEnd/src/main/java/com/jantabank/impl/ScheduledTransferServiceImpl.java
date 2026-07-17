package com.jantabank.impl;

import com.jantabank.domain.enums.ScheduleFrequency;
import com.jantabank.domain.enums.ScheduleStatus;
import com.jantabank.domain.enums.TransactionType;
import com.jantabank.domain.enums.TransferMode;
import com.jantabank.dto.txn.ScheduleTransferRequest;
import com.jantabank.dto.txn.ScheduledTransferDto;
import com.jantabank.dto.txn.TransferRequest;
import com.jantabank.entity.Account;
import com.jantabank.entity.ScheduledTransfer;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.AccountRepository;
import com.jantabank.repository.ScheduledTransferRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.MoneyTransferService;
import com.jantabank.service.ScheduledTransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledTransferServiceImpl implements ScheduledTransferService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTransferServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ScheduledTransferRepository scheduledTransferRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final MoneyTransferService moneyTransferService;

    public ScheduledTransferServiceImpl(ScheduledTransferRepository scheduledTransferRepository,
                                        AccountRepository accountRepository,
                                        UserRepository userRepository,
                                        MoneyTransferService moneyTransferService) {
        this.scheduledTransferRepository = scheduledTransferRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.moneyTransferService = moneyTransferService;
    }

    @Override
    @Transactional
    public ScheduledTransferDto schedule(ScheduleTransferRequest request, String username) {
        User user = loadUser(username);

        if (request.getAmount() <= 0) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Source and destination accounts must differ");
        }

        Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccountNumber());
        if (fromAccount == null) {
            throw new ResourceNotFoundException("Source account not found");
        }
        boolean owns = fromAccount.getUsers() != null && fromAccount.getUsers().stream()
                .anyMatch(u -> u.getId() == user.getId());
        if (!owns) {
            throw new TodoAPIException(HttpStatus.FORBIDDEN,
                    "You are not authorized to schedule transfers from this account");
        }
        if (request.getTransferMode() == TransferMode.WITHIN_BANK
                && accountRepository.findByAccountNumber(request.getToAccountNumber()) == null) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "Destination account not found for within-bank transfer");
        }

        ScheduledTransfer instruction = new ScheduledTransfer();
        instruction.setReferenceNumber(generateReference());
        instruction.setFromAccountNumber(request.getFromAccountNumber());
        instruction.setToAccountNumber(request.getToAccountNumber());
        instruction.setAmount(request.getAmount());
        instruction.setTransferMode(request.getTransferMode());
        instruction.setFrequency(request.getFrequency());
        instruction.setStatus(ScheduleStatus.SCHEDULED);
        instruction.setDescription(request.getDescription());
        instruction.setNextRunDate(request.getStartDate());
        instruction.setExecutionsCount(0);
        instruction.setInitiatedByUserId(user.getId());

        ScheduledTransfer saved = scheduledTransferRepository.save(instruction);
        log.info("Scheduled transfer {} ({} {}) created by userId={} startDate={}",
                saved.getReferenceNumber(), saved.getFrequency(), saved.getTransferMode(),
                user.getId(), saved.getNextRunDate());
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScheduledTransferDto> listMine(String username, Pageable pageable) {
        User user = loadUser(username);
        return scheduledTransferRepository
                .findByInitiatedByUserIdOrderByNextRunDateAsc(user.getId(), pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public ScheduledTransferDto cancel(Long id, String username) {
        User user = loadUser(username);
        ScheduledTransfer instruction = scheduledTransferRepository
                .findByIdAndInitiatedByUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled transfer not found"));
        if (instruction.getStatus() != ScheduleStatus.SCHEDULED) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "Only active scheduled transfers can be cancelled");
        }
        instruction.setStatus(ScheduleStatus.CANCELLED);
        ScheduledTransfer saved = scheduledTransferRepository.save(instruction);
        log.info("Scheduled transfer {} cancelled by userId={}", saved.getReferenceNumber(), user.getId());
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> dueInstructionIds() {
        return scheduledTransferRepository
                .findByStatusAndNextRunDateLessThanEqual(ScheduleStatus.SCHEDULED, LocalDate.now())
                .stream()
                .map(ScheduledTransfer::getId)
                .toList();
    }

    /**
     * Executes a single due instruction in its own transaction so one failure
     * neither rolls back nor blocks the others. Invoked through the Spring proxy
     * (from the scheduler task) so REQUIRES_NEW takes effect.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeInstruction(Long id) {
        ScheduledTransfer instruction = scheduledTransferRepository.findById(id).orElse(null);
        if (instruction == null || instruction.getStatus() != ScheduleStatus.SCHEDULED) {
            return;
        }
        User initiator = userRepository.findById(instruction.getInitiatedByUserId()).orElse(null);
        if (initiator == null) {
            instruction.setStatus(ScheduleStatus.FAILED);
            instruction.setLastError("Initiating user no longer exists");
            scheduledTransferRepository.save(instruction);
            return;
        }

        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber(instruction.getFromAccountNumber());
        request.setToAccountNumber(instruction.getToAccountNumber());
        request.setAmount(instruction.getAmount());
        request.setTransferMode(instruction.getTransferMode());
        request.setDescription(instruction.getDescription());

        TransactionType type = instruction.getFrequency() == ScheduleFrequency.ONCE
                ? TransactionType.SCHEDULED_TRANSFER : TransactionType.RECURRING_TRANSFER;

        try {
            moneyTransferService.executeTransfer(request, initiator, "SCHEDULER", type);
            instruction.setExecutionsCount(instruction.getExecutionsCount() + 1);
            instruction.setLastRunAt(LocalDateTime.now());
            instruction.setLastError(null);
            if (instruction.getFrequency() == ScheduleFrequency.ONCE) {
                instruction.setStatus(ScheduleStatus.COMPLETED);
            } else {
                instruction.setNextRunDate(advance(instruction.getNextRunDate(), instruction.getFrequency()));
            }
            log.info("Executed scheduled transfer {} (execution #{})",
                    instruction.getReferenceNumber(), instruction.getExecutionsCount());
        } catch (Exception e) {
            instruction.setStatus(ScheduleStatus.FAILED);
            instruction.setLastRunAt(LocalDateTime.now());
            instruction.setLastError(truncate(e.getMessage()));
            log.warn("Scheduled transfer {} failed: {}", instruction.getReferenceNumber(), e.getMessage());
        }
        scheduledTransferRepository.save(instruction);
    }

    private LocalDate advance(LocalDate from, ScheduleFrequency frequency) {
        switch (frequency) {
            case DAILY:
                return from.plusDays(1);
            case WEEKLY:
                return from.plusWeeks(1);
            case MONTHLY:
                return from.plusMonths(1);
            default:
                return from;
        }
    }

    private String generateReference() {
        for (int i = 0; i < 5; i++) {
            String candidate = "SCH" + System.currentTimeMillis() + String.format("%04d", RANDOM.nextInt(10000));
            boolean exists = scheduledTransferRepository.findAll().stream()
                    .anyMatch(s -> candidate.equals(s.getReferenceNumber()));
            if (!exists) {
                return candidate;
            }
        }
        throw new TodoAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not allocate a schedule reference");
    }

    private String truncate(String message) {
        if (message == null) {
            return "Execution failed";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ScheduledTransferDto toDto(ScheduledTransfer s) {
        return ScheduledTransferDto.builder()
                .id(s.getId())
                .referenceNumber(s.getReferenceNumber())
                .fromAccount(maskAccount(s.getFromAccountNumber()))
                .toAccount(maskAccount(s.getToAccountNumber()))
                .amount(s.getAmount())
                .transferMode(s.getTransferMode() != null ? s.getTransferMode().name() : null)
                .frequency(s.getFrequency() != null ? s.getFrequency().name() : null)
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .description(s.getDescription())
                .nextRunDate(s.getNextRunDate())
                .lastRunAt(s.getLastRunAt())
                .executionsCount(s.getExecutionsCount())
                .lastError(s.getLastError())
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
        return "XXXX" + accountNumber.substring(len - 4);
    }
}
