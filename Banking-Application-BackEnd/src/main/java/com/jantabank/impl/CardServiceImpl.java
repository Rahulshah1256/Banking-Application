package com.jantabank.impl;

import com.jantabank.domain.enums.CardAction;
import com.jantabank.domain.enums.CardNetwork;
import com.jantabank.domain.enums.CardStatus;
import com.jantabank.domain.enums.CardType;
import com.jantabank.dto.card.BlockCardRequest;
import com.jantabank.dto.card.CardControlsRequest;
import com.jantabank.dto.card.CardDto;
import com.jantabank.dto.card.CardHistoryDto;
import com.jantabank.dto.card.CardLimitsRequest;
import com.jantabank.dto.card.IssueCardRequest;
import com.jantabank.dto.card.SetPinRequest;
import com.jantabank.entity.Account;
import com.jantabank.entity.Card;
import com.jantabank.entity.CardHistory;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.AccountRepository;
import com.jantabank.repository.CardHistoryRepository;
import com.jantabank.repository.CardRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CardServiceImpl implements CardService {

    private static final Logger log = LoggerFactory.getLogger(CardServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_CARD_NUMBER_ATTEMPTS = 5;

    private final CardRepository cardRepository;
    private final CardHistoryRepository cardHistoryRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final double atmDefault;
    private final double posDefault;
    private final double onlineDefault;
    private final double atmMax;
    private final double posMax;
    private final double onlineMax;
    private final int validityYears;

    public CardServiceImpl(CardRepository cardRepository,
                           CardHistoryRepository cardHistoryRepository,
                           AccountRepository accountRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.card.atm-daily-limit-default:50000}") double atmDefault,
                           @Value("${app.card.pos-daily-limit-default:100000}") double posDefault,
                           @Value("${app.card.online-daily-limit-default:100000}") double onlineDefault,
                           @Value("${app.card.atm-daily-limit-max:100000}") double atmMax,
                           @Value("${app.card.pos-daily-limit-max:500000}") double posMax,
                           @Value("${app.card.online-daily-limit-max:500000}") double onlineMax,
                           @Value("${app.card.validity-years:5}") int validityYears) {
        this.cardRepository = cardRepository;
        this.cardHistoryRepository = cardHistoryRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.atmDefault = atmDefault;
        this.posDefault = posDefault;
        this.onlineDefault = onlineDefault;
        this.atmMax = atmMax;
        this.posMax = posMax;
        this.onlineMax = onlineMax;
        this.validityYears = validityYears;
    }

    @Override
    @Transactional
    public CardDto issue(IssueCardRequest request, String username) {
        User user = loadUser(username);
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber());
        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }
        assertOwnership(account, user);

        Card card = buildCard(account, user, request.getNetwork());
        Card saved = cardRepository.save(card);
        record(saved, CardAction.ISSUE, "Card issued on " + maskAccount(account.getAccountNumber()));
        log.info("Issued {} card {} for userId={}", request.getNetwork(),
                maskCard(saved.getCardNumber()), user.getId());
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardDto> listMine(String username) {
        User user = loadUser(username);
        return cardRepository.findByUserIdOrderByIdDesc(user.getId()).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto get(long id, String username) {
        return toDto(loadOwned(id, username));
    }

    @Override
    @Transactional
    public CardDto block(long id, BlockCardRequest request, String username) {
        Card card = loadOwned(id, username);
        assertNotReplaced(card);
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Card is already blocked");
        }
        card.setStatus(CardStatus.BLOCKED);
        card.setBlockedReason(request != null ? request.getReason() : null);
        Card saved = cardRepository.save(card);
        record(saved, CardAction.BLOCK, request != null ? request.getReason() : null);
        log.info("Blocked card {} (reason={})", maskCard(saved.getCardNumber()),
                request != null ? request.getReason() : "n/a");
        return toDto(saved);
    }

    @Override
    @Transactional
    public CardDto unblock(long id, String username) {
        Card card = loadOwned(id, username);
        assertNotReplaced(card);
        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Card is not blocked");
        }
        card.setStatus(CardStatus.ACTIVE);
        card.setBlockedReason(null);
        Card saved = cardRepository.save(card);
        record(saved, CardAction.UNBLOCK, null);
        log.info("Unblocked card {}", maskCard(saved.getCardNumber()));
        return toDto(saved);
    }

    @Override
    @Transactional
    public CardDto replace(long id, String username) {
        Card oldCard = loadOwned(id, username);
        assertNotReplaced(oldCard);
        User user = loadUser(username);

        oldCard.setStatus(CardStatus.REPLACED);
        cardRepository.save(oldCard);
        record(oldCard, CardAction.REPLACE, "Replaced by a new card");

        Account account = accountRepository.findByAccountNumber(oldCard.getAccountNumber());
        Card newCard = buildCard(account, user, oldCard.getNetwork());
        Card saved = cardRepository.save(newCard);
        record(saved, CardAction.ISSUE, "Issued as replacement for card id=" + oldCard.getId());
        log.info("Replaced card id={} -> new card {}", oldCard.getId(), maskCard(saved.getCardNumber()));
        return toDto(saved);
    }

    @Override
    @Transactional
    public CardDto setPin(long id, SetPinRequest request, String username) {
        Card card = loadOwned(id, username);
        assertNotReplaced(card);
        card.setPinHash(passwordEncoder.encode(request.getPin()));
        Card saved = cardRepository.save(card);
        record(saved, CardAction.PIN_SET, "PIN set/changed");
        log.info("PIN set for card {}", maskCard(saved.getCardNumber()));
        return toDto(saved);
    }

    @Override
    @Transactional
    public CardDto updateControls(long id, CardControlsRequest request, String username) {
        Card card = loadOwned(id, username);
        assertNotReplaced(card);
        StringBuilder changes = new StringBuilder();
        if (request.getInternationalEnabled() != null) {
            card.setInternationalEnabled(request.getInternationalEnabled());
            changes.append("international=").append(request.getInternationalEnabled()).append(' ');
        }
        if (request.getOnlineEnabled() != null) {
            card.setOnlineEnabled(request.getOnlineEnabled());
            changes.append("online=").append(request.getOnlineEnabled()).append(' ');
        }
        if (request.getContactlessEnabled() != null) {
            card.setContactlessEnabled(request.getContactlessEnabled());
            changes.append("contactless=").append(request.getContactlessEnabled()).append(' ');
        }
        Card saved = cardRepository.save(card);
        record(saved, CardAction.CONTROL_UPDATE, changes.toString().trim());
        log.info("Updated controls for card {}: {}", maskCard(saved.getCardNumber()), changes.toString().trim());
        return toDto(saved);
    }

    @Override
    @Transactional
    public CardDto updateLimits(long id, CardLimitsRequest request, String username) {
        Card card = loadOwned(id, username);
        assertNotReplaced(card);
        StringBuilder changes = new StringBuilder();
        if (request.getAtmDailyLimit() != null) {
            validateLimit("ATM", request.getAtmDailyLimit(), atmMax);
            card.setAtmDailyLimit(request.getAtmDailyLimit());
            changes.append("atm=").append(request.getAtmDailyLimit()).append(' ');
        }
        if (request.getPosDailyLimit() != null) {
            validateLimit("POS", request.getPosDailyLimit(), posMax);
            card.setPosDailyLimit(request.getPosDailyLimit());
            changes.append("pos=").append(request.getPosDailyLimit()).append(' ');
        }
        if (request.getOnlineDailyLimit() != null) {
            validateLimit("Online", request.getOnlineDailyLimit(), onlineMax);
            card.setOnlineDailyLimit(request.getOnlineDailyLimit());
            changes.append("online=").append(request.getOnlineDailyLimit()).append(' ');
        }
        Card saved = cardRepository.save(card);
        record(saved, CardAction.LIMIT_UPDATE, changes.toString().trim());
        log.info("Updated limits for card {}: {}", maskCard(saved.getCardNumber()), changes.toString().trim());
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardHistoryDto> history(long id, String username) {
        Card card = loadOwned(id, username);
        return cardHistoryRepository.findByCardIdOrderByIdDesc(card.getId()).stream()
                .map(h -> CardHistoryDto.builder()
                        .id(h.getId())
                        .action(h.getAction() != null ? h.getAction().name() : null)
                        .details(h.getDetails())
                        .timestamp(h.getCreatedAt())
                        .build())
                .toList();
    }

    private Card buildCard(Account account, User user, CardNetwork network) {
        LocalDateTime now = LocalDateTime.now();
        Card card = new Card();
        card.setCardNumber(generateCardNumber(network));
        card.setCardHolderName(account.getAccountHolderName());
        card.setAccountNumber(account.getAccountNumber());
        card.setUserId(user.getId());
        card.setCardType(CardType.DEBIT);
        card.setNetwork(network);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpiryMonth(now.getMonthValue());
        card.setExpiryYear(now.getYear() + validityYears);
        card.setCvvHash(passwordEncoder.encode(String.format("%03d", RANDOM.nextInt(1000))));
        card.setInternationalEnabled(false);
        card.setOnlineEnabled(true);
        card.setContactlessEnabled(true);
        card.setAtmDailyLimit(atmDefault);
        card.setPosDailyLimit(posDefault);
        card.setOnlineDailyLimit(onlineDefault);
        card.setIssuedAt(now);
        return card;
    }

    private void validateLimit(String label, double value, double max) {
        if (value > max) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    label + " daily limit exceeds the permitted maximum of " + max);
        }
    }

    private void assertNotReplaced(Card card) {
        if (card.getStatus() == CardStatus.REPLACED || card.getStatus() == CardStatus.EXPIRED) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "This card is " + card.getStatus().name().toLowerCase() + " and can no longer be modified");
        }
    }

    private void assertOwnership(Account account, User user) {
        boolean owns = account.getUsers() != null && account.getUsers().stream()
                .anyMatch(u -> u.getId() == user.getId());
        if (!owns) {
            throw new TodoAPIException(HttpStatus.FORBIDDEN, "You are not authorized to use this account");
        }
    }

    private void record(Card card, CardAction action, String details) {
        CardHistory history = new CardHistory();
        history.setCardId(card.getId());
        history.setAction(action);
        history.setDetails(details);
        cardHistoryRepository.save(history);
    }

    private String generateCardNumber(CardNetwork network) {
        for (int i = 0; i < MAX_CARD_NUMBER_ATTEMPTS; i++) {
            String candidate = buildPan(network.getPrefix());
            if (!cardRepository.existsByCardNumber(candidate)) {
                return candidate;
            }
        }
        throw new TodoAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not allocate a card number");
    }

    private String buildPan(String prefix) {
        StringBuilder sb = new StringBuilder(prefix);
        while (sb.length() < 15) {
            sb.append(RANDOM.nextInt(10));
        }
        sb.append(luhnCheckDigit(sb.toString()));
        return sb.toString();
    }

    private int luhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = number.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }

    private Card loadOwned(long id, String username) {
        User user = loadUser(username);
        return cardRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private CardDto toDto(Card c) {
        return CardDto.builder()
                .id(c.getId())
                .maskedCardNumber(maskCard(c.getCardNumber()))
                .cardHolderName(c.getCardHolderName())
                .maskedAccountNumber(maskAccount(c.getAccountNumber()))
                .cardType(c.getCardType() != null ? c.getCardType().name() : null)
                .network(c.getNetwork() != null ? c.getNetwork().name() : null)
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .expiry(String.format("%02d/%d", c.getExpiryMonth(), c.getExpiryYear()))
                .pinSet(c.getPinHash() != null)
                .internationalEnabled(c.isInternationalEnabled())
                .onlineEnabled(c.isOnlineEnabled())
                .contactlessEnabled(c.isContactlessEnabled())
                .atmDailyLimit(c.getAtmDailyLimit())
                .posDailyLimit(c.getPosDailyLimit())
                .onlineDailyLimit(c.getOnlineDailyLimit())
                .blockedReason(c.getBlockedReason())
                .issuedAt(c.getIssuedAt())
                .build();
    }

    private String maskCard(String pan) {
        if (pan == null || pan.length() < 4) {
            return pan;
        }
        return "XXXXXXXXXXXX" + pan.substring(pan.length() - 4);
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
