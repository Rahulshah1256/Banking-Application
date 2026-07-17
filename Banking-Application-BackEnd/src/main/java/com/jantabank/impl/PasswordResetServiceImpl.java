package com.jantabank.impl;

import com.jantabank.dto.ChangePasswordRequest;
import com.jantabank.dto.ForgotPasswordRequest;
import com.jantabank.dto.ResetPasswordRequest;
import com.jantabank.entity.PasswordResetToken;
import com.jantabank.entity.User;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.PasswordResetTokenRepository;
import com.jantabank.repository.RefreshTokenRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.security.TokenHashUtil;
import com.jantabank.service.EmailService;
import com.jantabank.service.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final int RESET_TOKEN_BYTES = 48;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final long resetExpirationMillis;

    public PasswordResetServiceImpl(UserRepository userRepository,
                                    PasswordResetTokenRepository passwordResetTokenRepository,
                                    RefreshTokenRepository refreshTokenRepository,
                                    PasswordEncoder passwordEncoder,
                                    EmailService emailService,
                                    @Value("${app.password-reset-expiration-milliseconds:1800000}") long resetExpirationMillis) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.resetExpirationMillis = resetExpirationMillis;
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new TodoAPIException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new TodoAPIException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(user.getId());
        log.info("Password changed for userId={}", user.getId());
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            log.info("Password-reset requested for non-existent email (suppressed): {}", request.getEmail());
            return;
        }
        User user = optionalUser.get();
        passwordResetTokenRepository.invalidateAllForUser(user.getId());

        String rawToken = TokenHashUtil.generateOpaqueToken(RESET_TOKEN_BYTES);
        PasswordResetToken token = new PasswordResetToken();
        token.setTokenHash(TokenHashUtil.sha256Hex(rawToken));
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusNanos(resetExpirationMillis * 1_000_000));
        token.setUsed(false);
        passwordResetTokenRepository.save(token);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), rawToken);
        log.info("Password-reset token issued for userId={}", user.getId());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(
                        TokenHashUtil.sha256Hex(request.getToken()))
                .orElseThrow(() -> new TodoAPIException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token"));

        if (token.isUsed()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "This reset token has already been used");
        }
        if (token.isExpired()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "This reset token has expired");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);
        passwordResetTokenRepository.invalidateAllForUser(user.getId());
        refreshTokenRepository.revokeAllForUser(user.getId());
        log.info("Password reset completed for userId={}", user.getId());
    }
}
