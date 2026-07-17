package com.jantabank.impl;

import com.jantabank.dto.LoginHistoryDto;
import com.jantabank.entity.LoginHistory;
import com.jantabank.entity.User;
import com.jantabank.repository.LoginHistoryRepository;
import com.jantabank.service.LoginHistoryService;
import com.jantabank.web.ClientMetadata;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class LoginHistoryServiceImpl implements LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(User user, String attemptedUsername, boolean successful, String failureReason, ClientMetadata metadata) {
        LoginHistory history = new LoginHistory();
        history.setUser(user);
        history.setAttemptedUsername(attemptedUsername);
        history.setSuccessful(successful);
        history.setFailureReason(failureReason);
        if (metadata != null) {
            history.setIpAddress(metadata.getIpAddress());
            history.setUserAgent(metadata.getUserAgent());
            history.setDeviceId(metadata.getDeviceId());
        }
        history.setLoginTime(LocalDateTime.now());
        loginHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoginHistoryDto> getHistory(long userId, Pageable pageable) {
        return loginHistoryRepository.findByUserIdOrderByLoginTimeDesc(userId, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginHistoryDto getLastSuccessfulLogin(long userId) {
        return loginHistoryRepository.findFirstByUserIdAndSuccessfulTrueOrderByLoginTimeDesc(userId)
                .map(this::toDto)
                .orElse(null);
    }

    private LoginHistoryDto toDto(LoginHistory h) {
        return LoginHistoryDto.builder()
                .id(h.getId())
                .successful(h.isSuccessful())
                .failureReason(h.getFailureReason())
                .ipAddress(h.getIpAddress())
                .userAgent(h.getUserAgent())
                .deviceId(h.getDeviceId())
                .loginTime(h.getLoginTime())
                .build();
    }
}
