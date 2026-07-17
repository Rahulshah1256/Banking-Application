package com.jantabank.impl;

import com.jantabank.entity.RefreshToken;
import com.jantabank.entity.User;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.RefreshTokenRepository;
import com.jantabank.security.TokenHashUtil;
import com.jantabank.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);
    private static final int REFRESH_TOKEN_BYTES = 48;

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationMillis;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                   @Value("${app.jwt-refresh-expiration-milliseconds}") long refreshExpirationMillis) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMillis = refreshExpirationMillis;
    }

    @Override
    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = TokenHashUtil.generateOpaqueToken(REFRESH_TOKEN_BYTES);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(TokenHashUtil.sha256Hex(rawToken));
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusNanos(refreshExpirationMillis * 1_000_000));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);
        log.debug("Issued refresh token for userId={}", user.getId());
        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verify(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256Hex(rawToken))
                .orElseThrow(() -> new TodoAPIException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (refreshToken.isRevoked()) {
            throw new TodoAPIException(HttpStatus.UNAUTHORIZED, "Refresh token has been revoked");
        }
        if (refreshToken.isExpired()) {
            throw new TodoAPIException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }
        return refreshToken;
    }

    @Override
    @Transactional
    public String rotate(String rawToken) {
        RefreshToken current = verify(rawToken);
        current.setRevoked(true);
        refreshTokenRepository.save(current);
        return createRefreshToken(current.getUser());
    }

    @Override
    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256Hex(rawToken)).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.debug("Revoked refresh token id={}", token.getId());
        });
    }
}
