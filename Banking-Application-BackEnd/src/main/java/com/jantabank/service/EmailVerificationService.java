package com.jantabank.service;

import com.jantabank.dto.ResendVerificationRequest;
import com.jantabank.dto.VerifyEmailRequest;
import com.jantabank.entity.User;

public interface EmailVerificationService {

    /** Issues a verification token for the user and dispatches the email. */
    void sendVerification(User user);

    /**
     * Re-issues a verification email for the account matching the email (if any
     * and not already verified). Always succeeds silently to avoid enumeration.
     */
    void resendVerification(ResendVerificationRequest request);

    /** Verifies an email using a valid, unused, unexpired token. */
    void verifyEmail(VerifyEmailRequest request);
}
