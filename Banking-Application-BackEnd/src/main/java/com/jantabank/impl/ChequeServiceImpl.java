package com.jantabank.impl;

import com.jantabank.domain.enums.ChequeBookStatus;
import com.jantabank.domain.enums.ChequeStatus;
import com.jantabank.dto.cheque.ChequeBookRequest;
import com.jantabank.dto.cheque.ChequeBookResponse;
import com.jantabank.dto.cheque.ChequeResponse;
import com.jantabank.dto.cheque.PositivePayRequest;
import com.jantabank.dto.cheque.StopChequeRequest;
import com.jantabank.entity.Account;
import com.jantabank.entity.Cheque;
import com.jantabank.entity.ChequeBook;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.AccountRepository;
import com.jantabank.repository.ChequeBookRepository;
import com.jantabank.repository.ChequeRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.ChequeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChequeServiceImpl implements ChequeService {

    private static final Logger log = LoggerFactory.getLogger(ChequeServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_ALLOC_ATTEMPTS = 6;
    private static final long CHEQUE_NUMBER_FLOOR = 100_000_000L;

    private final ChequeBookRepository chequeBookRepository;
    private final ChequeRepository chequeRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final Set<Integer> allowedLeaves;

    public ChequeServiceImpl(ChequeBookRepository chequeBookRepository,
                             ChequeRepository chequeRepository,
                             AccountRepository accountRepository,
                             UserRepository userRepository,
                             @Value("${app.cheque.allowed-leaves:10,25,50,100}") String allowedLeavesCsv) {
        this.chequeBookRepository = chequeBookRepository;
        this.chequeRepository = chequeRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.allowedLeaves = Arrays.stream(allowedLeavesCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public ChequeBookResponse requestBook(ChequeBookRequest request, String username) {
        User user = loadUser(username);
        if (!allowedLeaves.contains(request.getNumberOfLeaves())) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "Number of leaves must be one of " + allowedLeaves);
        }
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber());
        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }
        assertOwnership(account, user);

        long start = allocateStart(request.getNumberOfLeaves());
        long end = start + request.getNumberOfLeaves() - 1;

        ChequeBook book = new ChequeBook();
        book.setBookReferenceNumber(generateBookReference());
        book.setUserId(user.getId());
        book.setAccountNumber(account.getAccountNumber());
        book.setNumberOfLeaves(request.getNumberOfLeaves());
        book.setStartChequeNumber(start);
        book.setEndChequeNumber(end);
        book.setStatus(ChequeBookStatus.REQUESTED);
        book.setDeliveryAddress(request.getDeliveryAddress());
        book.setRequestedAt(LocalDateTime.now());
        book = chequeBookRepository.save(book);

        for (long number = start; number <= end; number++) {
            Cheque cheque = new Cheque();
            cheque.setChequeBookId(book.getId());
            cheque.setUserId(user.getId());
            cheque.setAccountNumber(account.getAccountNumber());
            cheque.setChequeNumber(number);
            cheque.setStatus(ChequeStatus.ACTIVE);
            cheque.setPositivePayRegistered(false);
            chequeRepository.save(cheque);
        }

        log.info("Cheque book {} requested for account {} ({} leaves {}-{})",
                book.getBookReferenceNumber(), account.getAccountNumber(),
                request.getNumberOfLeaves(), start, end);
        return toBookResponse(book, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChequeBookResponse> listBooks(String username) {
        User user = loadUser(username);
        return chequeBookRepository.findByUserIdOrderByIdDesc(user.getId()).stream()
                .map(b -> toBookResponse(b, false))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ChequeBookResponse getBook(Long bookId, String username) {
        User user = loadUser(username);
        ChequeBook book = chequeBookRepository.findByIdAndUserId(bookId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cheque book not found"));
        return toBookResponse(book, true);
    }

    @Override
    @Transactional
    public ChequeBookResponse issueBook(Long bookId) {
        ChequeBook book = chequeBookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Cheque book not found"));
        if (book.getStatus() != ChequeBookStatus.REQUESTED) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Only requested books can be issued");
        }
        book.setStatus(ChequeBookStatus.ISSUED);
        book.setIssuedAt(LocalDateTime.now());
        chequeBookRepository.save(book);
        log.info("Cheque book {} issued", book.getBookReferenceNumber());
        return toBookResponse(book, true);
    }

    @Override
    @Transactional
    public ChequeBookResponse deliverBook(Long bookId) {
        ChequeBook book = chequeBookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Cheque book not found"));
        if (book.getStatus() != ChequeBookStatus.ISSUED && book.getStatus() != ChequeBookStatus.DISPATCHED) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Only issued/dispatched books can be delivered");
        }
        book.setStatus(ChequeBookStatus.DELIVERED);
        book.setDeliveredAt(LocalDateTime.now());
        chequeBookRepository.save(book);
        log.info("Cheque book {} delivered", book.getBookReferenceNumber());
        return toBookResponse(book, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChequeResponse> history(String username, ChequeStatus status, String accountNumber) {
        User user = loadUser(username);
        String acct = (accountNumber != null && !accountNumber.isBlank()) ? accountNumber.trim() : null;
        return chequeRepository.search(user.getId(), status, acct).stream()
                .map(this::toChequeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ChequeResponse getCheque(Long chequeId, String username) {
        return toChequeResponse(loadOwnedCheque(chequeId, username));
    }

    @Override
    @Transactional
    public ChequeResponse stopCheque(Long chequeId, StopChequeRequest request, String username) {
        Cheque cheque = loadOwnedCheque(chequeId, username);
        if (cheque.getStatus() == ChequeStatus.STOPPED) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Cheque is already stopped");
        }
        if (cheque.getStatus() == ChequeStatus.CLEARED) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "A cleared cheque cannot be stopped");
        }
        cheque.setStatus(ChequeStatus.STOPPED);
        cheque.setStopReason(request.getReason().trim());
        cheque.setStoppedAt(LocalDateTime.now());
        chequeRepository.save(cheque);
        log.info("Cheque {} stopped ({})", cheque.getChequeNumber(), request.getReason());
        return toChequeResponse(cheque);
    }

    @Override
    @Transactional
    public ChequeResponse registerPositivePay(Long chequeId, PositivePayRequest request, String username) {
        Cheque cheque = loadOwnedCheque(chequeId, username);
        if (cheque.getStatus() != ChequeStatus.ACTIVE) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Positive pay can only be registered on active cheques");
        }
        cheque.setPositivePayRegistered(true);
        cheque.setPositivePayAmount(round2(request.getAmount()));
        cheque.setPositivePayPayee(request.getPayeeName().trim());
        cheque.setPositivePayDate(request.getChequeDate());
        chequeRepository.save(cheque);
        log.info("Positive pay registered for cheque {} amount {} payee {}",
                cheque.getChequeNumber(), request.getAmount(), request.getPayeeName());
        return toChequeResponse(cheque);
    }

    private long allocateStart(int leaves) {
        for (int i = 0; i < MAX_ALLOC_ATTEMPTS; i++) {
            long start = CHEQUE_NUMBER_FLOOR + (long) (RANDOM.nextDouble() * 800_000_000L);
            long end = start + leaves - 1;
            if (!chequeRepository.existsByChequeNumber(start) && !chequeRepository.existsByChequeNumber(end)) {
                return start;
            }
        }
        throw new TodoAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not allocate cheque numbers");
    }

    private String generateBookReference() {
        for (int i = 0; i < MAX_ALLOC_ATTEMPTS; i++) {
            String candidate = "CHQBK" + String.format("%010d", (long) (RANDOM.nextDouble() * 10_000_000_000L));
            if (!chequeBookRepository.existsByBookReferenceNumber(candidate)) {
                return candidate;
            }
        }
        throw new TodoAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not allocate a cheque book reference");
    }

    private void assertOwnership(Account account, User user) {
        boolean owns = account.getUsers() != null && account.getUsers().stream()
                .anyMatch(u -> u.getId() == user.getId());
        if (!owns) {
            throw new TodoAPIException(HttpStatus.FORBIDDEN, "You are not authorized to use this account");
        }
    }

    private Cheque loadOwnedCheque(Long id, String username) {
        User user = loadUser(username);
        return chequeRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cheque not found"));
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ChequeBookResponse toBookResponse(ChequeBook book, boolean includeLeaves) {
        List<ChequeResponse> leaves = null;
        if (includeLeaves) {
            leaves = new ArrayList<>(chequeRepository.findByChequeBookIdOrderByChequeNumberAsc(book.getId()).stream()
                    .map(this::toChequeResponse)
                    .toList());
        }
        return ChequeBookResponse.builder()
                .id(book.getId())
                .bookReferenceNumber(book.getBookReferenceNumber())
                .accountNumber(book.getAccountNumber())
                .numberOfLeaves(book.getNumberOfLeaves())
                .startChequeNumber(book.getStartChequeNumber())
                .endChequeNumber(book.getEndChequeNumber())
                .status(book.getStatus())
                .deliveryAddress(book.getDeliveryAddress())
                .requestedAt(book.getRequestedAt())
                .issuedAt(book.getIssuedAt())
                .deliveredAt(book.getDeliveredAt())
                .leaves(leaves)
                .build();
    }

    private ChequeResponse toChequeResponse(Cheque c) {
        return ChequeResponse.builder()
                .id(c.getId())
                .chequeBookId(c.getChequeBookId())
                .accountNumber(c.getAccountNumber())
                .chequeNumber(c.getChequeNumber())
                .status(c.getStatus())
                .stopReason(c.getStopReason())
                .stoppedAt(c.getStoppedAt())
                .positivePayRegistered(c.isPositivePayRegistered())
                .positivePayAmount(c.getPositivePayAmount())
                .positivePayPayee(c.getPositivePayPayee())
                .positivePayDate(c.getPositivePayDate())
                .build();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
