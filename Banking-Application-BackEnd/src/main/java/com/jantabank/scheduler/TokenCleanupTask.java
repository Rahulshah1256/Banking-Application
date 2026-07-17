package com.jantabank.scheduler;

import com.jantabank.repository.EmailVerificationTokenRepository;
import com.jantabank.repository.LoginOtpRepository;
import com.jantabank.repository.PasswordResetTokenRepository;
import com.jantabank.repository.RefreshTokenRepository;
import com.jantabank.repository.TokenBlacklistRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Periodically purges expired refresh tokens and blacklist entries so the
 * auth tables do not grow unbounded.
 */
@Component
@AllArgsConstructor
public class TokenCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupTask.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final LoginOtpRepository loginOtpRepository;

    @Scheduled(fixedRateString = "${app.token-cleanup-interval-milliseconds:3600000}")
    @Transactional
    public void purgeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteByExpiryDateBefore(now);
        tokenBlacklistRepository.deleteByExpiryDateBefore(now);
        passwordResetTokenRepository.deleteByExpiryDateBefore(now);
        emailVerificationTokenRepository.deleteByExpiryDateBefore(now);
        loginOtpRepository.deleteByExpiryDateBefore(now);
        log.debug("Purged expired auth tokens (refresh, blacklist, reset, verification, otp) at {}", now);
    }
}
