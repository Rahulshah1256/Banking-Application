package com.jantabank.service;

import com.jantabank.entity.User;

public interface LoginAttemptService {

    /** Increments the failed-attempt counter and locks the account past the threshold. */
    void recordFailure(long userId);

    /** Clears the failed-attempt counter and any lock after a successful login. */
    void recordSuccess(User user);

    /** Human-readable remaining lock duration message for a locked user. */
    String lockMessage(User user);
}
