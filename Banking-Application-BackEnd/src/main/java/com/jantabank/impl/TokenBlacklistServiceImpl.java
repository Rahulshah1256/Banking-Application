package com.jantabank.impl;

import com.jantabank.entity.TokenBlacklist;
import com.jantabank.repository.TokenBlacklistRepository;
import com.jantabank.service.TokenBlacklistService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistServiceImpl.class);

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    @Transactional
    public void blacklist(String jti, LocalDateTime expiry) {
        if (jti == null || jti.isBlank() || tokenBlacklistRepository.existsByJti(jti)) {
            return;
        }
        TokenBlacklist entry = new TokenBlacklist();
        entry.setJti(jti);
        entry.setExpiryDate(expiry != null ? expiry : LocalDateTime.now());
        tokenBlacklistRepository.save(entry);
        log.debug("Blacklisted access token jti={}", jti);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlacklisted(String jti) {
        return jti != null && tokenBlacklistRepository.existsByJti(jti);
    }
}
