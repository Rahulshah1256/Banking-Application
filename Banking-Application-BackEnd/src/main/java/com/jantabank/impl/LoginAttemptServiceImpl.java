package com.jantabank.impl;

import com.jantabank.entity.User;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.LoginAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptServiceImpl.class);

    private final UserRepository userRepository;
    private final int maxFailedAttempts;
    private final long lockDurationMillis;

    public LoginAttemptServiceImpl(UserRepository userRepository,
                                   @Value("${app.max-failed-login-attempts:5}") int maxFailedAttempts,
                                   @Value("${app.account-lock-duration-milliseconds:900000}") long lockDurationMillis) {
        this.userRepository = userRepository;
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockDurationMillis = lockDurationMillis;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= maxFailedAttempts) {
                user.setLockUntil(LocalDateTime.now().plusNanos(lockDurationMillis * 1_000_000));
                log.warn("Account locked for userId={} after {} failed attempts", userId, attempts);
            }
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public void recordSuccess(User user) {
        if (user.getFailedLoginAttempts() != 0 || user.getLockUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockUntil(null);
            userRepository.save(user);
        }
    }

    @Override
    public String lockMessage(User user) {
        if (user.getLockUntil() == null) {
            return "Account is temporarily locked.";
        }
        long minutes = Math.max(1, Duration.between(LocalDateTime.now(), user.getLockUntil()).toMinutes() + 1);
        return "Account is locked due to multiple failed login attempts. Try again in about "
                + minutes + " minute(s).";
    }
}
