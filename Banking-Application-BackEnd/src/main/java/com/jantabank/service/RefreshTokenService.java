package com.jantabank.service;

import com.jantabank.entity.RefreshToken;
import com.jantabank.entity.User;

public interface RefreshTokenService {

    /**
     * Issues a new refresh token for the user and returns the raw (unhashed)
     * value to be sent to the client. Only the hash is persisted.
     */
    String createRefreshToken(User user);

    /**
     * Validates a raw refresh token, ensuring it exists, is not revoked and not
     * expired. Throws if invalid.
     */
    RefreshToken verify(String rawToken);

    /**
     * Revokes the supplied refresh token and issues a fresh one for the same
     * user (rotation). Returns the new raw token value.
     */
    String rotate(String rawToken);

    /** Revokes the supplied refresh token if present (idempotent). */
    void revoke(String rawToken);
}
