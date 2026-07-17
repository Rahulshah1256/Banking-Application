package com.jantabank.service;

import com.jantabank.dto.LoginHistoryDto;
import com.jantabank.entity.User;
import com.jantabank.web.ClientMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoginHistoryService {

    /** Records a login attempt. {@code user} may be null for unknown usernames. */
    void record(User user, String attemptedUsername, boolean successful, String failureReason, ClientMetadata metadata);

    /** Returns paginated login history for a user, newest first. */
    Page<LoginHistoryDto> getHistory(long userId, Pageable pageable);

    /** Returns the most recent successful login for a user, or null if none. */
    LoginHistoryDto getLastSuccessfulLogin(long userId);
}
