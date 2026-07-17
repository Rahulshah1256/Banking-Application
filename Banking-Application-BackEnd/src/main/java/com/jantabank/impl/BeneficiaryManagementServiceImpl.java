package com.jantabank.impl;

import com.jantabank.domain.enums.BeneficiaryStatus;
import com.jantabank.dto.beneficiary.BeneficiaryDetailDto;
import com.jantabank.dto.beneficiary.CreateBeneficiaryRequest;
import com.jantabank.dto.beneficiary.UpdateBeneficiaryRequest;
import com.jantabank.entity.Account;
import com.jantabank.entity.Beneficiary;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.AccountRepository;
import com.jantabank.repository.BeneficiaryRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.BeneficiaryManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class BeneficiaryManagementServiceImpl implements BeneficiaryManagementService {

    private static final Logger log = LoggerFactory.getLogger(BeneficiaryManagementServiceImpl.class);

    private final BeneficiaryRepository beneficiaryRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final long activationDelayMinutes;

    public BeneficiaryManagementServiceImpl(BeneficiaryRepository beneficiaryRepository,
                                            AccountRepository accountRepository,
                                            UserRepository userRepository,
                                            @Value("${app.beneficiary.activation-delay-minutes:30}") long activationDelayMinutes) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.activationDelayMinutes = activationDelayMinutes;
    }

    @Override
    @Transactional
    public BeneficiaryDetailDto register(CreateBeneficiaryRequest request, String username) {
        User user = loadUser(username);
        List<Account> myAccounts = accountRepository.findByUsers_Id(user.getId());
        if (myAccounts.isEmpty()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "You have no account to link a beneficiary to");
        }

        String beneficiaryAccountNumber = request.getBeneficiaryAccountNumber().trim();
        if (!beneficiaryRepository.findOwnedByBeneficiaryAccountNumber(user.getId(), beneficiaryAccountNumber).isEmpty()) {
            throw new TodoAPIException(HttpStatus.CONFLICT, "This beneficiary already exists");
        }
        if (myAccounts.stream().anyMatch(a -> a.getAccountNumber().equals(beneficiaryAccountNumber))) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "You cannot add your own account as a beneficiary");
        }

        Account linkAccount = resolveLinkAccount(request.getLinkAccountNumber(), myAccounts);

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryaccountnumber(beneficiaryAccountNumber);
        beneficiary.setBeneficiaryaccountname(request.getBeneficiaryAccountName().trim());
        beneficiary.setBeneficiaryaccountifsc(request.getBeneficiaryAccountIfsc().trim().toUpperCase());
        beneficiary.setAmountlimit(request.getAmountLimit());
        beneficiary.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : null);
        beneficiary.setFavourite(false);
        beneficiary.setStatus(BeneficiaryStatus.PENDING);
        beneficiary.setActivateAfter(LocalDateTime.now().plusMinutes(activationDelayMinutes));
        beneficiary.setAccounts(Collections.singleton(linkAccount));

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiary {} registered (PENDING, activateAfter={}) by userId={}",
                maskAccount(beneficiaryAccountNumber), saved.getActivateAfter(), user.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public BeneficiaryDetailDto update(long id, UpdateBeneficiaryRequest request, String username) {
        User user = loadUser(username);
        Beneficiary beneficiary = loadOwned(id, user.getId());

        if (StringUtils.hasText(request.getBeneficiaryAccountName())) {
            beneficiary.setBeneficiaryaccountname(request.getBeneficiaryAccountName().trim());
        }
        beneficiary.setAmountlimit(request.getAmountLimit());
        beneficiary.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : null);

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiary id={} updated by userId={}", id, user.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public void delete(long id, String username) {
        User user = loadUser(username);
        Beneficiary beneficiary = loadOwned(id, user.getId());
        if (beneficiary.getAccounts() != null) {
            beneficiary.getAccounts().clear();
            beneficiaryRepository.saveAndFlush(beneficiary);
        }
        beneficiaryRepository.delete(beneficiary);
        log.info("Beneficiary id={} deleted by userId={}", id, user.getId());
    }

    @Override
    @Transactional
    public BeneficiaryDetailDto approve(long id, String username, boolean isAdmin) {
        User user = loadUser(username);
        Beneficiary beneficiary = loadOwned(id, user.getId());

        if (beneficiary.getStatus() == BeneficiaryStatus.ACTIVE) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Beneficiary is already active");
        }
        if (!isAdmin && beneficiary.getActivateAfter() != null
                && beneficiary.getActivateAfter().isAfter(LocalDateTime.now())) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "Activation delay has not elapsed; beneficiary can be approved after "
                            + beneficiary.getActivateAfter());
        }
        beneficiary.setStatus(BeneficiaryStatus.ACTIVE);
        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiary id={} approved (admin={}) by userId={}", id, isAdmin, user.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public BeneficiaryDetailDto toggleFavourite(long id, String username) {
        User user = loadUser(username);
        Beneficiary beneficiary = loadOwned(id, user.getId());
        beneficiary.setFavourite(!beneficiary.isFavourite());
        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiary id={} favourite={} by userId={}", id, saved.isFavourite(), user.getId());
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryDetailDto> search(String username, BeneficiaryStatus status,
                                             Boolean favourite, String query) {
        User user = loadUser(username);
        String q = StringUtils.hasText(query) ? query.trim() : null;
        return beneficiaryRepository.search(user.getId(), status, favourite, q)
                .stream().map(this::toDto).toList();
    }

    private Account resolveLinkAccount(String linkAccountNumber, List<Account> myAccounts) {
        if (StringUtils.hasText(linkAccountNumber)) {
            return myAccounts.stream()
                    .filter(a -> a.getAccountNumber().equals(linkAccountNumber.trim()))
                    .findFirst()
                    .orElseThrow(() -> new TodoAPIException(HttpStatus.BAD_REQUEST,
                            "Link account not found among your accounts"));
        }
        return myAccounts.get(0);
    }

    private Beneficiary loadOwned(long id, long userId) {
        return beneficiaryRepository.findByIdAndAccounts_Users_Id(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private BeneficiaryDetailDto toDto(Beneficiary b) {
        boolean payable = b.getStatus() == BeneficiaryStatus.ACTIVE;
        return BeneficiaryDetailDto.builder()
                .id(b.getId())
                .beneficiaryAccountNumber(b.getBeneficiaryaccountnumber())
                .maskedAccountNumber(maskAccount(b.getBeneficiaryaccountnumber()))
                .beneficiaryAccountName(b.getBeneficiaryaccountname())
                .beneficiaryAccountIfsc(b.getBeneficiaryaccountifsc())
                .amountLimit(b.getAmountlimit())
                .nickname(b.getNickname())
                .status(b.getStatus() != null ? b.getStatus().name() : null)
                .favourite(b.isFavourite())
                .payable(payable)
                .activateAfter(b.getActivateAfter())
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
