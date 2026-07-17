package com.jantabank.service;

import java.time.LocalDateTime;

public interface TokenBlacklistService {

    /** Blacklists an access token's jti until the given expiry. */
    void blacklist(String jti, LocalDateTime expiry);

    /** Returns true if the given jti has been revoked. */
    boolean isBlacklisted(String jti);
}
