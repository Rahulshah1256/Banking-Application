package com.jantabank.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.jantabank.domain.enums.TransactionType;
import com.jantabank.domain.enums.TransferMode;
import com.jantabank.dto.txn.TransactionReceiptDto;
import com.jantabank.dto.txn.TransferRequest;
import com.jantabank.dto.upi.RegisterUpiHandleRequest;
import com.jantabank.dto.upi.UpiHandleDto;
import com.jantabank.dto.upi.UpiPayRequest;
import com.jantabank.dto.upi.UpiResolveDto;
import com.jantabank.entity.Account;
import com.jantabank.entity.UpiHandle;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.AccountRepository;
import com.jantabank.repository.UpiHandleRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.MoneyTransferService;
import com.jantabank.service.UpiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class UpiServiceImpl implements UpiService {

    private static final Logger log = LoggerFactory.getLogger(UpiServiceImpl.class);
    private static final int QR_SIZE = 300;

    private final UpiHandleRepository upiHandleRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final MoneyTransferService moneyTransferService;

    public UpiServiceImpl(UpiHandleRepository upiHandleRepository,
                          AccountRepository accountRepository,
                          UserRepository userRepository,
                          MoneyTransferService moneyTransferService) {
        this.upiHandleRepository = upiHandleRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.moneyTransferService = moneyTransferService;
    }

    @Override
    @Transactional
    public UpiHandleDto registerHandle(RegisterUpiHandleRequest request, String username) {
        User user = loadUser(username);
        String vpa = request.getVpa().trim().toLowerCase();

        if (upiHandleRepository.existsByVpaIgnoreCase(vpa)) {
            throw new TodoAPIException(HttpStatus.CONFLICT, "This VPA is already taken");
        }

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber());
        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }
        assertOwnership(account, user);

        UpiHandle handle = new UpiHandle();
        handle.setVpa(vpa);
        handle.setAccountNumber(account.getAccountNumber());
        handle.setUserId(user.getId());
        handle.setActive(true);
        handle.setPrimary(request.isPrimary() || upiHandleRepository
                .findByUserIdOrderByPrimaryDescIdAsc(user.getId()).isEmpty());

        UpiHandle saved = upiHandleRepository.save(handle);
        log.info("Registered VPA {} -> account {} for userId={}", vpa,
                maskAccount(account.getAccountNumber()), user.getId());
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UpiHandleDto> listHandles(String username) {
        User user = loadUser(username);
        return upiHandleRepository.findByUserIdOrderByPrimaryDescIdAsc(user.getId())
                .stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UpiResolveDto resolve(String vpa) {
        UpiHandle handle = loadHandle(vpa);
        Account account = accountRepository.findByAccountNumber(handle.getAccountNumber());
        String payeeName = account != null ? account.getAccountHolderName() : null;
        return UpiResolveDto.builder()
                .vpa(handle.getVpa())
                .payeeName(payeeName)
                .maskedAccount(maskAccount(handle.getAccountNumber()))
                .active(handle.isActive())
                .build();
    }

    @Override
    @Transactional
    public TransactionReceiptDto pay(UpiPayRequest request, String username) {
        User user = loadUser(username);

        String fromAccountNumber = resolvePayerAccount(request, user);
        UpiHandle payee = loadHandle(request.getPayeeVpa());
        if (!payee.isActive()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Payee VPA is inactive");
        }

        TransferRequest transfer = new TransferRequest();
        transfer.setFromAccountNumber(fromAccountNumber);
        transfer.setToAccountNumber(payee.getAccountNumber());
        transfer.setAmount(request.getAmount());
        transfer.setTransferMode(TransferMode.UPI);
        transfer.setDescription(StringUtils.hasText(request.getNote())
                ? request.getNote() : "UPI payment to " + payee.getVpa());

        log.info("UPI pay of {} from {} to {} by userId={}", request.getAmount(),
                maskAccount(fromAccountNumber), payee.getVpa(), user.getId());
        return moneyTransferService.executeTransfer(transfer, user, "UPI", TransactionType.UPI);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateCollectQr(String vpa, Double amount, String note, String username) {
        User user = loadUser(username);
        UpiHandle handle = loadHandle(vpa);
        if (!handle.getUserId().equals(user.getId())) {
            throw new TodoAPIException(HttpStatus.FORBIDDEN,
                    "You can only generate QR codes for your own VPAs");
        }
        Account account = accountRepository.findByAccountNumber(handle.getAccountNumber());
        String payeeName = account != null ? account.getAccountHolderName() : handle.getVpa();

        StringBuilder payload = new StringBuilder("upi://pay?pa=")
                .append(encode(handle.getVpa()))
                .append("&pn=").append(encode(payeeName))
                .append("&cu=INR");
        if (amount != null && amount > 0) {
            payload.append("&am=").append(encode(String.format("%.2f", amount)));
        }
        if (StringUtils.hasText(note)) {
            payload.append("&tn=").append(encode(note));
        }
        return renderQr(payload.toString());
    }

    private String resolvePayerAccount(UpiPayRequest request, User user) {
        if (StringUtils.hasText(request.getPayerVpa())) {
            UpiHandle payer = loadHandle(request.getPayerVpa());
            if (!payer.getUserId().equals(user.getId())) {
                throw new TodoAPIException(HttpStatus.FORBIDDEN,
                        "You are not authorized to pay from this VPA");
            }
            return payer.getAccountNumber();
        }
        if (StringUtils.hasText(request.getFromAccountNumber())) {
            Account account = accountRepository.findByAccountNumber(request.getFromAccountNumber());
            if (account == null) {
                throw new ResourceNotFoundException("Source account not found");
            }
            assertOwnership(account, user);
            return account.getAccountNumber();
        }
        throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                "Either payerVpa or fromAccountNumber is required");
    }

    private byte[] renderQr(String content) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate UPI QR code", e);
            throw new TodoAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate QR code");
        }
    }

    private UpiHandle loadHandle(String vpa) {
        if (!StringUtils.hasText(vpa)) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "VPA is required");
        }
        return upiHandleRepository.findByVpaIgnoreCase(vpa.trim())
                .orElseThrow(() -> new ResourceNotFoundException("VPA not found"));
    }

    private void assertOwnership(Account account, User user) {
        boolean owns = account.getUsers() != null && account.getUsers().stream()
                .anyMatch(u -> u.getId() == user.getId());
        if (!owns) {
            throw new TodoAPIException(HttpStatus.FORBIDDEN,
                    "You are not authorized to use this account");
        }
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UpiHandleDto toDto(UpiHandle h) {
        return UpiHandleDto.builder()
                .id(h.getId())
                .vpa(h.getVpa())
                .maskedAccount(maskAccount(h.getAccountNumber()))
                .primary(h.isPrimary())
                .active(h.isActive())
                .build();
    }

    private String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
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
