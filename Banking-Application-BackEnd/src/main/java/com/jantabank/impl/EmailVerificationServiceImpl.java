package com.jantabank.impl;

import com.jantabank.dto.ResendVerificationRequest;
import com.jantabank.dto.VerifyEmailRequest;
import com.jantabank.entity.EmailVerificationToken;
import com.jantabank.entity.User;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.EmailVerificationTokenRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.security.TokenHashUtil;
import com.jantabank.service.EmailService;
import com.jantabank.service.EmailVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);
    private static final int VERIFICATION_TOKEN_BYTES = 48;

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final long verificationExpirationMillis;

    public EmailVerificationServiceImpl(UserRepository userRepository,
                                        EmailVerificationTokenRepository tokenRepository,
                                        EmailService emailService,
                                        @Value("${app.email-verification-expiration-milliseconds:86400000}") long verificationExpirationMillis) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.verificationExpirationMillis = verificationExpirationMillis;
    }

    @Override
    @Transactional
    public void sendVerification(User user) {
        tokenRepository.invalidateAllForUser(user.getId());

        String rawToken = TokenHashUtil.generateOpaqueToken(VERIFICATION_TOKEN_BYTES);
        EmailVerificationToken token = new EmailVerificationToken();
        token.setTokenHash(TokenHashUtil.sha256Hex(rawToken));
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusNanos(verificationExpirationMillis * 1_000_000));
        token.setUsed(false);
        tokenRepository.save(token);

        emailService.sendEmailVerification(user.getEmail(), user.getName(), rawToken);
        log.info("Email-verification token issued for userId={}", user.getId());
    }

    @Override
    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            log.info("Verification resend requested for non-existent email (suppressed): {}", request.getEmail());
            return;
        }
        User user = optionalUser.get();
        if (user.isEmailVerified()) {
            log.info("Verification resend requested for already-verified userId={} (suppressed)", user.getId());
            return;
        }
        sendVerification(user);
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        EmailVerificationToken token = tokenRepository.findByTokenHash(
                        TokenHashUtil.sha256Hex(request.getToken()))
                .orElseThrow(() -> new TodoAPIException(HttpStatus.BAD_REQUEST, "Invalid or expired verification token"));

        if (token.isUsed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "This verification token has already been used");
        }
        if (token.isExpired()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "This verification token has expired");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
        tokenRepository.invalidateAllForUser(user.getId());
        log.info("Email verified for userId={}", user.getId());
    }
}
